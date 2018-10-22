package konishi.evaluation_software;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class AppController {

	@FXML private ImageView video_frame;
	@FXML private Label description_label;
	@FXML private Label image_path_label;
	@FXML private TextField frame_num_text;
	@FXML private TextField evaluation_text;

	@FXML private GridPane image_pane;
	@FXML private AnchorPane base_pane;


	String imageExtension = "jpg";

	String video_path = "";
	String save_dir = "";
	String save_csv_path = "";

	//String sample_path = "/konishi/evaluation_software/Octocat.png";

	AnimationTimer timer = null;

	/**
	 * 現在読み込み中のフレーム数
	 */
	int nowLoadingFrameNum = 0;
	
	/**
	 * 全フレーム数
	 */
	int totalFrameNum = 0;

	/**
	 * 動画を切り出すフレーム間隔
	 * デフォルトでは1000フレーム毎
	 */
	int frameInterval = 1000;

	/**
	 * 推定残り時間
	 */
	double estimatedTime = 0;

	/**
	 * 現在何番目の画像を見ているかを示す
	 */
	int location = 0;
	
	/**
	 * 動画切り出し中かを示すフラグ
	 */
	boolean isLoading = false;

	HashMap<Integer, Integer> evaluation_map = null;

	public void initialize() {

		evaluation_map = new HashMap<Integer, Integer>();

		//Image image = new Image(sample_path);
		//video_frame.setImage(image);
		frame_num_text.setText("" + frameInterval);


		/*
		 * 軽い処理＆GUIコンポーネントを操作する場合はアニメーションタイマーで処理
		 * (重たい処理はGUIが固まるので注意)
		 */
		timer = new AnimationTimer() {	
			@Override
			public void handle(long now) {
				video_frame.setFitHeight(image_pane.getHeight()*0.9);
				video_frame.setFitWidth(image_pane.getWidth());
			}
		};
		timer.start();

	}



	@FXML public void handleSelectVideoMenu(ActionEvent e) throws IOException {
		System.out.println("[SELECT] Select Video");

		FileChooser fc = new FileChooser();
		File importFile = fc.showOpenDialog(null);
		
		if (importFile == null) {
			System.out.println("No select");
			return;
		}

		video_path = "file:" + importFile.getPath();

		description_label.setText("Set video-path: " + video_path);

	}

	@FXML public void handleSelectImageDirectoryMenu(ActionEvent e) throws IOException {
		System.out.println("[SELECT] Select Image Directory");

		DirectoryChooser dc = new DirectoryChooser();
		File importDir = dc.showDialog(null);
		
		if (importDir == null) {
			System.out.println("No select");
			return;
		}
		
		save_dir = importDir.getPath() + File.separator;
		clearEnvironment();
	}


	@FXML public void handleEvaluationText(KeyEvent e) {
		String text = "";
		System.out.println("[CONTROL] InputEvaluationText");

		text = evaluation_text.getText();
		System.out.println("text: " + text);

		if (isNumber(text)) {
			evaluation_map.put(location, Integer.parseInt(text));
			System.out.println("Key: " + location + ", Value: " + evaluation_map.get(location));
		} else if (text.isEmpty()) {
			if (evaluation_map.get(location) != null) {
				evaluation_map.remove(location);
			}
		}
	}

	@FXML public void handleSaveCsvMenu(ActionEvent e) throws IOException {
		System.out.println("[SELECT] Save CSV");

		if (getFilteredFiles(save_dir, imageExtension).length != evaluation_map.size()) {
			description_label.setText("There are some Empty-Values, Can't Save!");
			return;
		}
		
		if (video_path.isEmpty()) {
			description_label.setText("Select Video-Path");
			return;
		}

		DirectoryChooser dc = new DirectoryChooser();
		File importDir = dc.showDialog(null);
		
		String f = new File(video_path).getName();
		save_csv_path = importDir + File.separator + f.substring(0,f.lastIndexOf('.')) + ".csv";

		convertToCsv(evaluation_map);

		description_label.setText("Wrote in \"" + save_csv_path + "\"");

	}

	/**
	 *  HashMapをCSVに変換
	 * @param map
	 * @throws IOException
	 */
	public void convertToCsv(HashMap<Integer, Integer> map) throws IOException {
		File f = new File(save_csv_path);
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));

		pw.println("Frame-Number,Evaluation-Value");
		for (int key : map.keySet()) {
			System.out.println(key + " => " + map.get(key));
			pw.println(key*frameInterval + "," + map.get(key));
		}
		pw.close();
	}
	
	public void clearEnvironment() {
		System.out.println("[CLEAR] Clear Environment");
		
		evaluation_map.clear();
		evaluation_text.setText("");
		location = 0;
		updateImage();
	}


	@FXML public void handleMakeFrameButton(MouseEvent mouse) {
		System.out.println("[CONTROL] Press MakeFrameButton");

		String frameText = frame_num_text.getText();
		if (!isNumber(frameText)) {
			description_label.setText("Input Number in the \"Frame.\"");
			frame_num_text.setText("" + frameInterval);
			return;
		} else if (video_path == "") {
			description_label.setText("Select Video-Path");
			return;
		} else if (save_dir == "") {
			description_label.setText("Select Save-Image-Directory.");
			return;
		}

		frameInterval = Integer.parseInt(frameText);

		/*
		 * 軽い処理＆GUIコンポーネントを操作する場合はアニメーションタイマーで処理
		 * (重たい処理はGUIが固まるので注意)
		 */
		timer = new AnimationTimer() {	
			@Override
			public void handle(long now) {
				String time;
				if (isLoading) {
					if (nowLoadingFrameNum % 100 == 0) {
						time = String.format("%.1f", estimatedTime);
						description_label.setText("Now Loading (" + nowLoadingFrameNum + "/" + totalFrameNum + "),  " + time + "s remaining");
					}
					if (nowLoadingFrameNum != 0 && nowLoadingFrameNum >= totalFrameNum-100) {
						description_label.setText("Loading Finished!");
						clearEnvironment();
					}
				}
			}
		};
		timer.start();

		/*
		 * 重い処理＆GUIコンポーネントを操作しない場合はスレッドに分けて処理
		 */
		new Thread(new Runnable() {
			public void run(){
				isLoading = true;
				try {
					get_frame(frameInterval);
				} catch (IOException e) {
					e.printStackTrace();
				}
				isLoading = false;

			}
		}).start();
	}

	public String[] sortFiles(File[] files) {
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) {
			list.add(files[i].getName());
		}
		Collections.sort(list);
		String[] fileArray = (String[])list.toArray(new String[list.size()]);

		return fileArray;
	}


	public File[] getFilteredFiles(String dir_path, final String extension) {

		if (dir_path.isEmpty()) {
			description_label.setText("Select Save-Image-Directory");
			return null;
		}

		File dir = new File(dir_path);


		// 拡張子フィルタの作成
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File file, String str){
				return str.endsWith(extension) ? true : false;
			}
		};
		
		// ディレクトリにファイルが存在しない場合、空の配列(files.length == 0)が返却される
		File[] files = dir.listFiles(filter);

		return files;
	}

	public void updateImage() {
		File[] files = getFilteredFiles(save_dir, imageExtension);
		if (files.length == 0) {
			System.out.println("No Files");
			return;
		}

		String[] fileList = sortFiles(files);
		System.out.println("[SHOW] " + fileList[location]);
		video_frame.setImage(new Image("file:" + save_dir + fileList[location]));
		image_path_label.setText(fileList[location]);
	}

	@FXML public void handleNextButton(MouseEvent mouse) {
		System.out.println("[CONTROL] Press NextButton");

		File[] files = getFilteredFiles(save_dir, imageExtension);
		if (files == null)	return;

		if (location != files.length-1) {
			location++;
		}

		// 評価値を保持する
		if (evaluation_map.get(location) != null) {
			evaluation_text.setText("" + evaluation_map.get(location));
		} else {
			evaluation_text.setText("");
		}

		updateImage();
	}

	@FXML public void handleBeforeButton(MouseEvent mouse) {
		System.out.println("[CONTROL] Press BeforeButton");

		if (location != 0) {
			location--;
		}

		// 評価値を保持する
		if (evaluation_map.get(location) != null) {
			evaluation_text.setText("" + evaluation_map.get(location));
		} else {
			evaluation_text.setText("");
		}

		updateImage();
	}

	public void get_frame(int interval) throws IOException {

		FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(video_path);
		Java2DFrameConverter frameConverter = new Java2DFrameConverter();

		frameGrabber.start();

		totalFrameNum = frameGrabber.getLengthInFrames();

		// FFmpegFrameGrabber#startを実行した後から、getFrameRateやgetLengthInFramesなどのプロパティで値が取得できるようになる 
		System.out.println("format:" + frameGrabber.getFormat() + ", size:" + frameGrabber.getImageWidth() + "x"
				+ frameGrabber.getImageHeight() + ", frame-rate:" + frameGrabber.getFrameRate()
				+ ", length-in-frame:" + totalFrameNum);

		int frameBeforeNum = 0;
		long start = 0, end = 0, grabEnd = 0, time = 0;
		while (frameGrabber.getFrameNumber() != frameGrabber.getLengthInFrames()-1) {
			start = System.currentTimeMillis();

			Frame frame = frameGrabber.grab();
			nowLoadingFrameNum = frameGrabber.getFrameNumber();

			grabEnd = System.currentTimeMillis();

			if (nowLoadingFrameNum % interval == 0 && (nowLoadingFrameNum == 0 || frameBeforeNum != nowLoadingFrameNum)) {
				System.out.println("Now Frame: " + nowLoadingFrameNum + "/" + totalFrameNum);
			} else {
				continue;
			}
			// フレームを取りだす
			BufferedImage img = frameConverter.convert(frame);
			if (img == null)	continue;

			// 静止画で出力
			int digit = Integer.toString(frameGrabber.getLengthInFrames()).length();
			ImageIO.write(img, imageExtension, new File(save_dir, String.format("%0" + digit + "d", nowLoadingFrameNum) + "." + imageExtension));
			frameBeforeNum = nowLoadingFrameNum;

			end = System.currentTimeMillis();
			time = (totalFrameNum-nowLoadingFrameNum)*(grabEnd-start) + ((totalFrameNum-nowLoadingFrameNum)/interval)*(end-start);
			estimatedTime = (double)time/1000;
			System.out.println("Estimated Time: " + estimatedTime + "s");

		}

		frameGrabber.stop();
		frameGrabber.close();

		System.out.println("[FINISH] Load Video");
		nowLoadingFrameNum = 0;
		timer.stop();


		/*
		 * Alt+F5: Maven更新
		 */
	}

	public static BufferedImage IplImageToBufferedImage(IplImage src) {
		OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		Frame frame = grabberConverter.convert(src);
		return paintConverter.getBufferedImage(frame,1);
	}

	public boolean isNumber(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
