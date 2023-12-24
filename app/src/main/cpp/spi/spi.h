//
// Created by jethinr on 11/19/2018.
//

#ifndef DEMO_SPI_H
#define DEMO_SPI_H

#include <jni.h>

#ifdef __cplusplus

extern "C" {
#endif

JNIEXPORT jobject JNICALL Java_demo_qualcomm_spi_spiApplications_open
        (JNIEnv *, jclass, jstring, jint, jint, jint,jint);

JNIEXPORT void JNICALL Java_demo_qualcomm_spi_spiApplications_close
  (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_demo_qualcomm_spi_spiApplications_transfer
        (JNIEnv *, jclass,jint, jbyteArray, jbyteArray, jint);

JNIEXPORT jint JNICALL Java_demo_qualcomm_spi_spiApplications_configuremode
        (JNIEnv *, jclass, jint,jint);

#ifdef __cplusplus
}
#endif

#endif //DEMO_SPI_H
