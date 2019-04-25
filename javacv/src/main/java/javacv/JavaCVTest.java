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
        grabber.start();   //��ʼ��ȡ����ͷ����
        CanvasFrame canvas = new CanvasFrame("����ͷ");//�½�һ������
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
        while (true) {
            if (!canvas.isDisplayable()) {//�����Ƿ�ر�
                grabber.stop();//ֹͣץȡ
                System.exit(-1);//�˳�
            }

            Frame frame = grabber.grab();

            canvas.showImage(frame);//��ȡ����ͷͼ�񲢷ŵ���������ʾ�� �����Frame frame=grabber.grab(); frame��һ֡��Ƶͼ��
            Thread.sleep(50);//50����ˢ��һ��ͼ��
        }
    }

    @Test
    public void testCamera1() throws FrameGrabber.Exception, InterruptedException {
        VideoInputFrameGrabber grabber = VideoInputFrameGrabber.createDefault(0);
        grabber.start();
        CanvasFrame canvasFrame = new CanvasFrame("����ͷ");
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
		FrameGrabber grabber = FrameGrabber.createDefault(0);//��������ͷĬ��0������ʹ��javacv��ץȡ��������ʹ�õ���ffmpeg����opencv�������в鿴Դ��
		grabber.start();//����ץȡ��
 
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//ת����
		IplImage grabbedImage = converter.convert(grabber.grab());//ץȡһ֡��Ƶ������ת��Ϊͼ�����������ͼ��������ʲô����ˮӡ������ʶ��ȵ��������
		int width = grabbedImage.width();
		int height = grabbedImage.height();
	
		FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264������
		recorder.setFormat("flv");//��װ��ʽ����������͵�rtmp�ͱ�����flv��װ��ʽ
		recorder.setFrameRate(frameRate);		
		recorder.start();//����¼����
		
		long startTime=0;
		long videoTS=0;
		CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		//Frame rotatedFrame=converter.convert(grabbedImage);//��֪��Ϊʲô���ﲻ��ת���Ͳ����Ƶ�rtmp
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
