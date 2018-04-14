package com.bonult.naiveBayes;

import com.oracle.tools.packager.Log;
import weibo4j.Comments;
import weibo4j.Oauth;
import weibo4j.model.Comment;
import weibo4j.model.CommentWapper;
import weibo4j.model.Paging;
import weibo4j.model.WeiboException;
import weibo4j.util.BareBonesBrowserLaunch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by bonult on 2018/4/7.
 */
public class GetComments {
	public static void main(String[] args) throws Exception{
		Oauth oauth = new Oauth();
		BareBonesBrowserLaunch.openURL(oauth.authorize("code"));
		System.out.print("Hit enter when it's done.[Enter]:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String code = br.readLine();
		String accessToken = null;
		Log.info("code: " + code);
		try{
			accessToken = oauth.getAccessTokenByCode(code).getAccessToken();
		}catch(WeiboException e){
			if(401 == e.getStatusCode()){
				Log.info("Unable to get the access token.");
			}else{
				e.printStackTrace();
			}
		}
//		String accessToken = "2.00RYnwEERPWRQCe98bfe759bD8amAC";
		Comments cm = new Comments(accessToken);
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("cmt4.txt", true)))){
			for(int i = 1; i < 100; i++){
				try{
					CommentWapper comment = cm.getCommentById("4208668520715775", new Paging(i, 200), 0);
					List<Comment> l = comment.getComments();
					for(Comment c : l){
						if(!c.getText().startsWith("回复@")){
							pw.println(c.getText());
						}
					}
					System.out.println(l.size());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	private static String getSenti(String arg){
		try{
			String resp = sendPost("http://www.xunsearch.com/scws/demo/v4.php" + "?mydata=" + URLEncoder.encode(arg, "gbk") + "&limit=10&xattr=%7Ev", "");
			int start = resp.indexOf("<textarea cols=60 rows=14 class=demotx readonly style=\"color:#888;\">");
			int end = resp.indexOf("</textarea>", start);
			return resp.substring(start + 69, end).trim();
		}catch(Exception e){
			return null;
		}
	}

	private static String sendPost(String url, String Params) throws IOException{
		OutputStream out = null;
		BufferedReader reader = null;
		String response = "";
		try{
			URL httpUrl = null;
			httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "text/html");
			conn.setRequestProperty("connection", "keep-alive");
			conn.connect();
			out = conn.getOutputStream();
			out.write(Params.getBytes("gbk"));
			out.flush();
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "gbk"));
			String lines;
			while((lines = reader.readLine()) != null){
				response += lines + '\n';
			}
			reader.close();
			conn.disconnect();
		}catch(Exception e){
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}finally{
			try{
				if(out != null){
					out.close();
				}
				if(reader != null){
					reader.close();
				}
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
		return response;
	}

	public static void 分词(String[] args){
		String path = NaiveBayes.class.getClassLoader().getResource("").getPath();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + "评论.txt"))); PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path + "分词.txt")))){
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(getSenti(line));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main2(String[] args){
		String path = NaiveBayes.class.getClassLoader().getResource("").getPath();
		int[] randomArr = randomArray(0,1127,200);
		List<String> list = new ArrayList<>(1128);
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + "分词.txt")))){
			String line = null;
			while((line = br.readLine()) != null){
				list.add(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		for(int i : randomArr){
			System.out.println(list.get(i));
		}
	}

	public static int[] randomArray(int start, int end, int num){
		int len = end - start + 1;
		if(start > end || num > len){
			return null;
		}
		int[] source = new int[len];
		for(int i = 0; i < len; i++){
			source[i] = i + start;
		}
		int[] target = new int[num];
		Random rd = new Random();
		for(int i = 0; i < target.length; i++){
			int index = rd.nextInt(len--);
			target[i] = source[index];
			source[index] = source[len];
		}
		return target;
	}
}
