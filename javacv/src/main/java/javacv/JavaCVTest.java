package javacv;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.junit.Test;

public class JavaCVTest {
	@Test
    public void testCamera() throws InterruptedException, FrameGrabber.Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();   //开始获取摄像头数据
        CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
        while (true) {
            if (!canvas.isDisplayable()) {//窗口是否关闭
                grabber.stop();//停止抓取
                System.exit(-1);//退出
            }

            Frame frame = grabber.grab();

            canvas.showImage(frame);//获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab(); frame是一帧视频图像
            Thread.sleep(50);//50毫秒刷新一次图像
        }
    }

    @Test
    public void testCamera1() throws FrameGrabber.Exception, InterruptedException {
        VideoInputFrameGrabber grabber = VideoInputFrameGrabber.createDefault(0);
        grabber.start();
        CanvasFrame canvasFrame = new CanvasFrame("摄像头");
        canvasFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);
        while (true) {
            if (!canvasFrame.isDisplayable()) {
                grabber.stop();
                System.exit(-1);
            }
            Frame frame = grabber.grab();
            canvasFrame.showImage(frame);
            Thread.sleep(30);
        }
    }
    
    public static void recordCamera(String outputFile, double frameRate)
			throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {
		Loader.load(opencv_objdetect.class);
		FrameGrabber grabber = FrameGrabber.createDefault(0);//本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码
		grabber.start();//开启抓取器
 
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器
		IplImage grabbedImage = converter.convert(grabber.grab());//抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加
		int width = grabbedImage.width();
		int height = grabbedImage.height();
	
		FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
		recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式
		recorder.setFrameRate(frameRate);		
		recorder.start();//开启录制器
		
		long startTime=0;
		long videoTS=0;
		CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		//Frame rotatedFrame=converter.convert(grabbedImage);//不知道为什么这里不做转换就不能推到rtmp
		while (true) {
            if (!frame.isDisplayable()) {
            	recorder.release();
                grabber.stop();
                recorder.stop();
                System.exit(-1);
            }
            Frame rotatedFrame = grabber.grab();
			//rotatedFrame = converter.convert(grabbedImage);
			frame.showImage(rotatedFrame);
			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			videoTS = 1000 * (System.currentTimeMillis() - startTime);
			recorder.setTimestamp(videoTS);
			recorder.record(rotatedFrame);
			Thread.sleep(40);
		}
//		frame.dispose();
//		recorder.stop();
//		recorder.release();
//		grabber.stop();
	
	}

	public static void main(String[] args) throws org.bytedeco.javacv.FrameRecorder.Exception, InterruptedException, Exception {
		recordCamera("output.mp4",25);
		recordCamera("rtmp://192.168.30.21/live/record1",25);
	}
}
