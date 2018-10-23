# 実験評価用ソフトウェア

Date: 2018/10/24 (Latest Update)  
Author: konishi

-----------------------------

## 実行環境
- Java JRE-8(Version 1.8)以降
- Linux, Windows10 動作確認済み


## 実行方法
コマンドプロンプト(ターミナル)で、本ソフトウェア(.jar)があるディレクトリ(フォルダ)まで移動し、
`java -jar [本ソフトウェア名].jar` と入力することで実行できます。  
ダブルクリックでも実行可能ですが、システムログ(GUIでは見れないログ表示)を表示させるためにも、できるだけコマンドプロンプトから起動してください。


## 操作方法
### File -> Select Video
フレームを切り出す動画を選択します。
既に動画は画像に切りだされており、フレーム切り出し処理が必要無い場合も選択する必要があります。

### File -> Select Save-Image-Directory
切り出した画像・既に切りだされている画像が入っているディレクトリを選択します。

### File -> Save as CSV
評価値を記録したCSVファイルを保存するディレクトリを選択します。
CSVファイルの名前は、「[動画ファイル名].csv」となります。
全ての画像に対して、評価値を記入が終わらないと保存することはできません。

### [BUTTON] MakeFrame
読み込んだ動画の切り出し処理を開始します。
切り出し間隔(フレーム間隔)は、右のFrame欄に書かれた間隔(default:1000)となります。
切り出しを開始するには、動画の選択(Select Video)と画像の保存先を指定(Select Save-Image-Directory)が完了している必要があります。



## 使用時の注意事項
- 本ソフトウェア(.jar)は、プラットフォームに依存せず、ライブラリや開発環境を必要としませんが、
このファイルにライブラリを抽出しているため、ファイルサイズが約1.2GBと大変大きくなっています。

- 動画を読み込み、切り出し処理を行うため、ある程度のPCスペックは必要と思われます。

- 切り出したフレームは、全て画像化し、指定したディレクトリに保存するため、PCには空き容量が十分にあることも確認しておく必要があります。


## Javaインストール方法
### Windows
- [Java公式サイト](https://java.com/ja/download/)から最新のJREをインストールする
- コマンドプロンプトなどで、`java -version`と入力し、Version-1.8以上であることを確認する


### Linux
- 最新バージョンをインストールするため、apt-getなどでインストールしない
- Windowsと同様に[Java公式サイト](https://java.com/ja/download/)にアクセスし、「.tar.gz」などの圧縮ファイルを入手する
- 任意のディレクトリ(ex. /usr/java/)を作成し、そこに解凍する(.tar.gzならば、`tar -zxvf [file.tar.gz]`)。
- bashrcなどにパス(ex. /usr/java/[解凍したディレクトリ]/bin/)を登録する
- ターミナルで`java -version`とし、確認する

----------------------------------

以下では、開発における内容を説明する。

## 使用ライブラリ
- JavaCV 1.4.3 (インストールにはMaven使用)
- JavaFX 2.4.0 SDK

## 開発上の注意点
- JavaCV 1.4.3は、ffmpegにより動画をフレーム単位で読み込むことなどができるが、Mavenでxmlを記述し、インストールすることが必須(2018/10現在)
- Mavenのpom.xmlに必要なライブラリを記述後は、「Alt+F5」でMavenプロジェクトの更新を行うとよい
- JavaFXのGUIの開発には、SceneBuilderを用いると良い。SceneBuilderならマウスドラッグなどで視覚的にコンポーネントを配置できる。

以下にMavenのpom.xmlを記載する(パッケージ名などは変更する必要があるので注意)

## pom.xml

Mavenによるライブラリを実行可能jarに含めるためには、maven-assembly-pluginが必要となる。(以下のコードの最初のplugin)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>konishi.evaluation_software</groupId>
	<artifactId>evaluation_software</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>evaluation_software</name>
	<url>http://maven.apache.org</url>

	<build>
		<plugins>
			<!-- 実行可能jarファイル用のプラグイン -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-assembly-plugin</artifactId>
				<version>3.0.0</version>
				<configuration>
					<finalName>test</finalName>
					<descriptorRefs>
						<!-- 依存するリソースをすべてjarに同梱する -->
						<descriptorRef>jar-with-dependencies</descriptorRef>
					</descriptorRefs>
					<archive>
						<manifest>
							<mainClass>konishi.evaluation_software.Main</mainClass>
						</manifest>
					</archive>
				</configuration>
				<executions>
					<execution>
						<!-- idタグは任意の文字列であれば何でもよい -->
						<id>make-assembly</id>
						<phase>package</phase>
						<goals>
							<goal>single</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>


	<dependencies>
		<dependency>
			<groupId>org.bytedeco</groupId>
			<artifactId>javacpp</artifactId>
			<version>1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco</groupId>
			<artifactId>javacv</artifactId>
			<version>1.4.3</version>
		</dependency>

		<dependency>
			<groupId>org.bytedeco</groupId>
			<artifactId>javacpp</artifactId>
			<version>1.4.3</version>
		</dependency>

		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>opencv-platform</artifactId>
			<version>3.4.3-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>ffmpeg-platform</artifactId>
			<version>4.0.2-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>flycapture-platform</artifactId>
			<version>2.11.3.121-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>spinnaker-platform</artifactId>
			<version>1.15.0.63-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>libdc1394-platform</artifactId>
			<version>2.2.5-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>libfreenect-platform</artifactId>
			<version>0.5.3-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>libfreenect2-platform</artifactId>
			<version>0.2.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>librealsense-platform</artifactId>
			<version>1.12.1-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>videoinput-platform</artifactId>
			<version>0.200-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>artoolkitplus-platform</artifactId>
			<version>2.3.1-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>chilitags-platform</artifactId>
			<version>master-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>flandmark-platform</artifactId>
			<version>1.07-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>hdf5-platform</artifactId>
			<version>1.10.3-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>mkl-platform</artifactId>
			<version>2019.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>mkl-dnn-platform</artifactId>
			<version>0.16-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>openblas-platform</artifactId>
			<version>0.3.3-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>arpack-ng-platform</artifactId>
			<version>3.6.3-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>cminpack-platform</artifactId>
			<version>1.3.6-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>fftw-platform</artifactId>
			<version>3.3.8-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>gsl-platform</artifactId>
			<version>2.5-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>cpython-platform</artifactId>
			<version>3.6-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>llvm-platform</artifactId>
			<version>7.0.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>libpostal-platform</artifactId>
			<version>1.1-alpha-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>leptonica-platform</artifactId>
			<version>1.76.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>tesseract-platform</artifactId>
			<version>4.0.0-rc2-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>caffe-platform</artifactId>
			<version>1.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>cuda-platform</artifactId>
			<version>10.0-7.3-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>mxnet-platform</artifactId>
			<version>1.3.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>tensorflow-platform</artifactId>
			<version>1.11.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>tensorrt-platform</artifactId>
			<version>5.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>ale-platform</artifactId>
			<version>0.6.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>onnx-platform</artifactId>
			<version>1.3.0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>liquidfun-platform</artifactId>
			<version>20170717-43d53e0-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>skia-platform</artifactId>
			<version>20170511-53d6729-1.4.3</version>
		</dependency>
		<dependency>
			<groupId>org.bytedeco.javacpp-presets</groupId>
			<artifactId>systems-platform</artifactId>
			<version>1.4.3</version>
		</dependency>
	</dependencies>
</project>
```
