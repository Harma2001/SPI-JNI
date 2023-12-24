#include <linux/spi/spidev.h>
#include <cstdlib>
#include <cstring>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>

#include "spi.h"

#include "android/log.h"

static const char *TAG="spi_dev";
int spi_fd[5];
static struct spi_ioc_transfer g_spi_xfr = {0};
static uint8_t g_bpw = 8;
static uint32_t g_speed = 1000000;
static uint16_t g_delay = 0;
static int g_toggle_cs = 0;

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static void prepare_buffer(char * data, int len)
{
	struct spi_ioc_transfer *xfr = &g_spi_xfr;
	char *txbuf = (char*)malloc(len);
	char *rxbuf = (char*)malloc(len);

	memcpy(txbuf, data, len);
	memset(rxbuf, 0xff, len);	

	xfr->tx_buf        = (unsigned long)txbuf;
	xfr->rx_buf        = (unsigned long)rxbuf;
	xfr->len           = len;
	xfr->speed_hz      = g_speed;
	xfr->delay_usecs   = g_delay;
	xfr->bits_per_word = g_bpw;
	xfr->cs_change     = g_toggle_cs;
	xfr->pad           = 0;
}

static void free_buffer()
{
	struct spi_ioc_transfer *xfr = &g_spi_xfr;
	free((void*)xfr->tx_buf);
	free((void*)xfr->rx_buf);
}

static void transfer(int fd)
{
	int ret;
	ret = ioctl(fd, SPI_IOC_MESSAGE(1), &g_spi_xfr);
	if (ret < 0)
		LOGE("Can't send spi message");
}

JNIEXPORT jobject JNICALL Java_demo_qualcomm_spi_spiApplications_open
  (JNIEnv *env, jclass thiz, jstring path, jint speed, jint flags, jint mode,jint idx)
{
	jobject mFileDescriptor;
    char tx_buff[10];
    int ret = 0;

    const char *gpio_path = env->GetStringUTFChars(path, NULL);
    spi_fd[idx] = open(gpio_path, O_RDWR);
    LOGD("Opening SPI %s with flags 0x%x, fd = %d", gpio_path, O_RDWR, spi_fd[idx]);
    env->ReleaseStringUTFChars(path, gpio_path);
    if (spi_fd[idx] == -1)
    {
        LOGE("Unable to Open SPI");
        return NULL;
    }

	/* Configure device */
	{
		/*
		 * spi mode
		 */
		ret = ioctl(spi_fd[idx], SPI_IOC_WR_MODE, &mode);
		if (ret == -1)
			LOGE("Can't set SPI mode");

		ret = ioctl(spi_fd[idx], SPI_IOC_RD_MODE, &mode);
		if (ret == -1)
			LOGE("Can't get SPI mode");
		/*
		 * bits per word
		 */
		ret = ioctl(spi_fd[idx], SPI_IOC_WR_BITS_PER_WORD, &g_bpw);
		if (ret == -1)
			LOGE("Can't set bits per word");

		ret = ioctl(spi_fd[idx], SPI_IOC_RD_BITS_PER_WORD, &g_bpw);
		if (ret == -1)
			LOGE("Can't get bits per word");

		/*
		 * max speed hz
		 */
		g_speed = speed;
		ret = ioctl(spi_fd[idx], SPI_IOC_WR_MAX_SPEED_HZ, &speed);
		if (ret == -1)
			LOGE("Can't set max speed hz");

		ret = ioctl(spi_fd[idx], SPI_IOC_RD_MAX_SPEED_HZ, &speed);
		if (ret == -1)
			LOGE("Can't get max speed hz");
	}

    /* Creating file descriptor for Java*/
    {
        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
        mFileDescriptor = env->NewObject(cFileDescriptor, env->GetMethodID(cFileDescriptor, "<init>", "()V"));
        env->SetIntField(mFileDescriptor, env->GetFieldID(cFileDescriptor, "descriptor", "I"), (jint)spi_fd[idx]);
    }

	return mFileDescriptor;
}


JNIEXPORT void JNICALL Java_demo_qualcomm_spi_spiApplications_close
  (JNIEnv *env, jobject thiz)
{
	jclass SPIClass = env->GetObjectClass(thiz);
	jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

	jfieldID mFdID = env->GetFieldID(SPIClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

	jobject mFileDescriptor = env->GetObjectField(thiz, mFdID);
	jint descriptor = env->GetIntField(mFileDescriptor, descriptorID);

	LOGD("close(fd = %d)", descriptor);
	close(descriptor);
}

/*
 * Class:     android_serialport_SerialPort
 * Method:    transfer
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
 
JNIEXPORT jint JNICALL Java_demo_qualcomm_spi_spiApplications_transfer
  (JNIEnv *env, jclass thiz,jint idx, jbyteArray arrTx, jbyteArray arrRx, jint len)
{
	/*Getting Input and Output Buffers*/
	struct spi_ioc_transfer *xfr = &g_spi_xfr;
    jsize lenTx = env->GetArrayLength(arrTx);
    jsize lenRx = env->GetArrayLength(arrRx);
    jbyte *tx_buff = env->GetByteArrayElements(arrTx, 0);
    jbyte *rx_buff = env->GetByteArrayElements(arrRx, 0);
	
	if(lenTx<len) len = lenTx;
	if(lenRx<len) len = lenRx;
	prepare_buffer((char*)tx_buff,len);
	transfer(spi_fd[idx]);
	memcpy(rx_buff,(char*)xfr->rx_buf,len);
	free_buffer();
	
    env->ReleaseByteArrayElements(arrTx, tx_buff, 0);
    env->ReleaseByteArrayElements(arrRx, rx_buff, 0);

	return 0;
}
JNIEXPORT jint JNICALL Java_demo_qualcomm_spi_spiApplications_configuremode
        (JNIEnv *env, jclass thiz,jint idx, jint mode)
{
    jint ret = 0;
    ret = ioctl(spi_fd[idx], SPI_IOC_WR_MODE, &mode);
    if (ret == -1)
        LOGE("Can't set SPI mode");

    ret = ioctl(spi_fd[idx], SPI_IOC_RD_MODE, &mode);
    if (ret == -1)
        LOGE("Can't get SPI mode");
    return ret;
}



