//
// Created by Administrator on 2017/2/14.
//
#ifndef _Included_cn_usbfacedetect_main_USBCameraUtil
#define _Included_cn_usbfacedetect_main_USBCameraUtil


#include <errno.h>  
#include <sys/types.h>      
#include <sys/stat.h>   
#include <fcntl.h>  
#include <sys/ioctl.h>  
#include <stdio.h>
#include <linux/videodev2.h>
#include <sys/mman.h>  
#include <jni.h>
#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif

#define  LOG_TAG    "FimcGzsd"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOG_TAG, __VA_ARGS__)

struct fimc_buffer {
    unsigned char *start;
    size_t length;
};

static int fd = -1;
struct fimc_buffer *buffers = NULL;
struct v4l2_buffer v4l2_buf;
static int bufnum = 1;
static int mwidth, mheight;


int *rgb = NULL;
int *ybuf = NULL;

int yuv_tbl_ready = 0;
int y1192_tbl[256];
int v1634_tbl[256];
int v833_tbl[256];
int u400_tbl[256];
int u2066_tbl[256];

/*
 *open usb camera device
 * cn_usbfacedetect_main_USBCameraUtil
 * 
 */
JNIEXPORT jint JNICALL Java_cn_usbfacedetect_main_USBCameraUtil_open(JNIEnv *env, jclass obj, jint devid) {
    char *devname;
    switch (devid) {
        case 0:
            devname = "/dev/video0";
            break;
        case 11:
            devname = "/dev/video11";
            break;
        case 4:
            devname = "/dev/video4";
            break;
        default:
            devname = "/dev/video4";
            break;
    }

    fd = open(devname, O_RDWR, 0);

    if (fd < 0)
        LOGE("%s ++++ open error\n", devname);


    return fd;
}

/**
 * 通过传进byte打开camera
 */
JNIEXPORT jint JNICALL
Java_cn_usbfacedetect_main_USBCameraUtil_open2(JNIEnv *env, jclass obj, jbyteArray devName) {
    jbyte *dev = env->GetByteArrayElements(devName, NULL);
    fd = open((char *) dev, O_RDWR, 0);
    if (fd < 0) {
        LOGE("%s ++++ open error\n", devName);
        return -1;
    }
    env->ReleaseByteArrayElements(devName, dev, 0);
    return fd;
}

/*
 * init device
 */
JNIEXPORT jint JNICALL Java_cn_usbfacedetect_main_USBCameraUtil_init(JNIEnv *env, jclass obj, jint width,
                                                             jint height, jint numbuf) {
    int ret;
    int i;
    bufnum = numbuf;
    mwidth = width;
    mheight = height;
    struct v4l2_format fmt;
    struct v4l2_capability cap;

    ret = ioctl(fd, VIDIOC_QUERYCAP, &cap);
    if (ret < 0) {
        LOGE("%d :VIDIOC_QUERYCAP failed\n", __LINE__);
        return -1;
    }
    if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
        LOGE("%d : no capture devices\n", __LINE__);
        return -1;
    }

    memset(&fmt, 0, sizeof(fmt));
    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    fmt.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV;
    fmt.fmt.pix.width = width;
    fmt.fmt.pix.height = height;
    if (ioctl(fd, VIDIOC_S_FMT, &fmt) < 0) {
        LOGE("++++%d : set format failed\n", __LINE__);
        return -1;
    }

    struct v4l2_requestbuffers req;
    req.count = numbuf;
    req.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    req.memory = V4L2_MEMORY_MMAP;

    ret = ioctl(fd, VIDIOC_REQBUFS, &req);
    if (ret < 0) {
        LOGE("++++%d : VIDIOC_REQBUFS failed\n", __LINE__);
        return -1;
    }

    buffers = (fimc_buffer *) calloc(req.count, sizeof(*buffers));
    if (!buffers) {
        LOGE ("++++%d Out of memory\n", __LINE__);
        return -1;
    }

    for (i = 0; i < bufnum; ++i) {
        memset(&v4l2_buf, 0, sizeof(v4l2_buf));
        v4l2_buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        v4l2_buf.memory = V4L2_MEMORY_MMAP;
        v4l2_buf.index = i;
        ret = ioctl(fd, VIDIOC_QUERYBUF, &v4l2_buf);
        if (ret < 0) {
            LOGE("+++%d : VIDIOC_QUERYBUF failed\n", __LINE__);
            return -1;
        }
        buffers[i].length = v4l2_buf.length;
        buffers[i].start = (unsigned char *) mmap(0, v4l2_buf.length,
                                                  PROT_READ | PROT_WRITE, MAP_SHARED,
                                                  fd, v4l2_buf.m.offset);
        if (buffers[i].start < 0) {
            LOGE("%d : mmap() failed", __LINE__);
            return -1;
        }
    }

    rgb = (int *) malloc(sizeof(int) * (mwidth * mheight));
    ybuf = (int *) malloc(sizeof(int) * (mwidth * mheight));

    return 0;
}

/*
 *stream on
 */
JNIEXPORT jint JNICALL Java_cn_usbfacedetect_main_USBCameraUtil_streamon(JNIEnv *env, jclass obj) {
    int i;
    int ret;
    enum v4l2_buf_type type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    for (i = 0; i < bufnum; ++i) {
        memset(&v4l2_buf, 0, sizeof(v4l2_buf));
        v4l2_buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        v4l2_buf.memory = V4L2_MEMORY_MMAP;
        v4l2_buf.index = i;
        ret = ioctl(fd, VIDIOC_QBUF, &v4l2_buf);
        if (ret < 0) {
            LOGE("%d : VIDIOC_QBUF failed\n", __LINE__);
            return ret;
        }
    }
    ret = ioctl(fd, VIDIOC_STREAMON, &type);
    if (ret < 0) {
        LOGE("%d : VIDIOC_STREAMON failed\n", __LINE__);
        return ret;
    }
    return 0;
}
/*
 *get one frame data
 */
JNIEXPORT jint JNICALL Java_cn_usbfacedetect_main_USBCameraUtil_dqbuf(JNIEnv *env, jclass obj,
                                                              const jbyteArray videodata) {
    int ret;
    jbyte *data = env->GetByteArrayElements(videodata, 0);
    v4l2_buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    v4l2_buf.memory = V4L2_MEMORY_MMAP;

    ret = ioctl(fd, VIDIOC_DQBUF, &v4l2_buf);
    if (ret < 0) {
        LOGE("%s : VIDIOC_DQBUF failed, dropped frame\n", __func__);
        return ret;
    }
    memcpy(data, buffers[v4l2_buf.index].start, buffers[v4l2_buf.index].length);
    env->ReleaseByteArrayElements(videodata, data, 0);
    return v4l2_buf.index;
}

/*
 *put in frame buffer to queue
 */
JNIEXPORT jint JNICALL Java_cn_usbfacedetect_main_USBCameraUtil_qbuf(JNIEnv *env, jclass obj, jint index) {
    int ret;

    v4l2_buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    v4l2_buf.memory = V4L2_MEMORY_MMAP;
    v4l2_buf.index = index;

    ret = ioctl(fd, VIDIOC_QBUF, &v4l2_buf);
    if (ret < 0) {
        LOGE("%s : VIDIOC_QBUF failed\n", __func__);
        return ret;
    }

    return 0;
}

/*
 *streamoff
 */
JNIEXPORT jint JNICALL
Java_cn_usbfacedetect_main_USBCameraUtil_streamoff(JNIEnv *env, jclass obj, jint index) {
    enum v4l2_buf_type type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    int ret;

    ret = ioctl(fd, VIDIOC_STREAMOFF, &type);
    if (ret < 0) {
        LOGE("%s : VIDIOC_STREAMOFF failed\n", __func__);
        return ret;
    }

    return 0;
}
/*
 *release
 */
JNIEXPORT jint JNICALL Java_cn_usbfacedetect_main_USBCameraUtil_release(JNIEnv *env, jclass obj) {
    enum v4l2_buf_type type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    int ret;
    int i;

    ret = ioctl(fd, VIDIOC_STREAMOFF, &type);
    if (ret < 0) {
        LOGE("%s : VIDIOC_STREAMOFF failed\n", __func__);
        return ret;
    }

    for (i = 0; i < bufnum; i++) {
        ret = munmap(buffers[i].start, buffers[i].length);
        if (ret < 0) {
            LOGE("%s : munmap failed\n", __func__);
            return ret;
        }
    }
    free(buffers);
    close(fd);
    return 0;
}


void yuyv422torgb(unsigned char *src, unsigned int *mrgb) {

    int width = 0;
    int height = 0;

    width = mwidth;
    height = mheight;

    int frameSize = width * height * 2;

    int i;

    if ((!rgb || !ybuf)) {
        return;
    }
    int *lrgb = NULL;
    int *lybuf = NULL;

    lrgb = (int *) mrgb;
    lybuf = &ybuf[0];

    if (yuv_tbl_ready == 0) {
        for (i = 0; i < 256; i++) {
            y1192_tbl[i] = 1192 * (i - 16);
            if (y1192_tbl[i] < 0) {
                y1192_tbl[i] = 0;
            }

            v1634_tbl[i] = 1634 * (i - 128);
            v833_tbl[i] = 833 * (i - 128);
            u400_tbl[i] = 400 * (i - 128);
            u2066_tbl[i] = 2066 * (i - 128);
        }
        yuv_tbl_ready = 1;
    }

    for (i = 0; i < frameSize; i += 4) {
        unsigned char y1, y2, u, v;
        y1 = src[i];
        u = src[i + 1];
        y2 = src[i + 2];
        v = src[i + 3];

        int y1192_1 = y1192_tbl[y1];
        int r1 = (y1192_1 + v1634_tbl[v]) >> 10;
        int g1 = (y1192_1 - v833_tbl[v] - u400_tbl[u]) >> 10;
        int b1 = (y1192_1 + u2066_tbl[u]) >> 10;

        int y1192_2 = y1192_tbl[y2];
        int r2 = (y1192_2 + v1634_tbl[v]) >> 10;
        int g2 = (y1192_2 - v833_tbl[v] - u400_tbl[u]) >> 10;
        int b2 = (y1192_2 + u2066_tbl[u]) >> 10;

        r1 = r1 > 255 ? 255 : r1 < 0 ? 0 : r1;
        g1 = g1 > 255 ? 255 : g1 < 0 ? 0 : g1;
        b1 = b1 > 255 ? 255 : b1 < 0 ? 0 : b1;
        r2 = r2 > 255 ? 255 : r2 < 0 ? 0 : r2;
        g2 = g2 > 255 ? 255 : g2 < 0 ? 0 : g2;
        b2 = b2 > 255 ? 255 : b2 < 0 ? 0 : b2;

        *lrgb++ = 0xff000000 | r1 << 16 | g1 << 8 | b1;
        *lrgb++ = 0xff000000 | r2 << 16 | g2 << 8 | b2;

        if (lybuf != NULL) {
            *lybuf++ = y1;
            *lybuf++ = y2;
        }
    }

}

JNIEXPORT void JNICALL
Java_cn_usbfacedetect_main_USBCameraUtil_yuvtorgb2(JNIEnv *env, jclass obj, const jbyteArray yuvdata,
                                           jintArray rgbdata) {
    jbyte *ydata = env->GetByteArrayElements(yuvdata, 0);
    jint *rdata = env->GetIntArrayElements(rgbdata, 0);
    yuyv422torgb((unsigned char *) ydata, (unsigned int *) rdata);


    env->ReleaseByteArrayElements(yuvdata, ydata, 0);
    env->ReleaseIntArrayElements(rgbdata, rdata, 0);
//    return NULL;
}


#ifdef __cplusplus
}
#endif

#endif //OPENTEST_CAMERA_UTIL_H