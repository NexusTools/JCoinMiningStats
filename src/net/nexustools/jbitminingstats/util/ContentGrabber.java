package net.nexustools.jbitminingstats.util;

import java.io.IOException;
import java.io.InputStreamReader;

import net.nexustools.jbitminingstats.activity.MiningStatisticsActivity;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ContentGrabber {
	public static final int BUFFER_SIZE = 512;
	
	public static String fetch(String request) throws IOException {
		StringBuffer sb = new StringBuffer();
		HttpGet httpGet = new HttpGet(request);
		if(MiningStatisticsActivity.settings.getHTTPUserAgent() != null)
			httpGet.setHeader("User-Agent", MiningStatisticsActivity.settings.getHTTPUserAgent());
		HttpEntity ent = new DefaultHttpClient().execute(httpGet).getEntity();
		InputStreamReader reader = new InputStreamReader(ent.getContent());
		char[] data = new char[BUFFER_SIZE];
		int read = -1;
		while((read = reader.read(data)) != -1)
			sb.append(data, 0, read);
		return sb.toString();
	}
}
