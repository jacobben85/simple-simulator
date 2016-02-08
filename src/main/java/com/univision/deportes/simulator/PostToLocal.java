package com.univision.deportes.simulator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 * Post XML to server
 */
public class PostToLocal {

    private String postUrl = "http://sports.dev.y.univision.com/feeds/xml-team";
    private Long sleepTime = 100L;

    private String getXMLTeamURL(String url) throws IOException {

        URI uri = URI.create(url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("Authorization", "Basic dW5pdmlzaW9uOmM0Ymwz");
        httpGet.setURI(uri);
        HttpResponse response = client.execute(httpGet);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private List<String> getSportsMLDocURLs(String manifest) {

        List<String> urls = new ArrayList<String>();
        try {
            // Convert to a document.
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(manifest.getBytes()));

            // Pull all the file paths from the manifest.
            XPath xPath =  XPathFactory.newInstance().newXPath();
            String pathExpression = "//document-listing/file-path";
            NodeList nodeList = null;
            nodeList = (NodeList) xPath.compile(pathExpression).evaluate(document, XPathConstants.NODESET);

            // Go through the manifest and add paths to the list.
            for (int index = 0; index < nodeList.getLength(); index++) {
                Node node = nodeList.item(index);
                urls.add(node.getTextContent());
            }
            System.out.println("url count at this point : " + urls.size());

            // Page through the manifest if there is more.
            String nextExpression = "//metadata/next/text()";
            String next = (String) xPath.compile(nextExpression).evaluate(document, XPathConstants.STRING);

            // Recursively call next page.
            if (next != null && !next.isEmpty()) {
                System.out.println(next);
                urls.addAll( getSportsMLDocURLs(getXMLTeamURL(next)) );
            }

        } catch (XPathExpressionException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        }

        return urls;
    }

    private String generateFileName(String feedUrl) {
        List<String> filename = Arrays.asList(feedUrl.split("/"));
        String xmlname = filename.get(6);
        xmlname.substring(0, xmlname.length() - 4);
        return xmlname.substring(0, xmlname.length() - 4) + ".xml";
    }

    public void fetchLinksAndProcess(String url) throws IOException, InterruptedException {
        String response = getXMLTeamURL(url);
        List<String> feedUrls = getSportsMLDocURLs(response);

        System.out.println("total items count : " + feedUrls.size());
        processLinks(feedUrls);
    }

    void processLinks(List<String> feedUrls) throws IOException, InterruptedException {

        for (String feedUrl : feedUrls) {
            String feedResponse = getXMLTeamURL("http://staging.xmlteam.com/sportsml/files/" + feedUrl);
            InputStream stream = new ByteArrayInputStream(feedResponse.getBytes(StandardCharsets.UTF_8));
            String filename = generateFileName(feedUrl);

            System.out.println(filename);

            Long startTime = new Date().getTime();

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(this.postUrl);
            HttpEntity entity;

            post.setHeader("User-Agent", USER_AGENT);
            post.setHeader("Content-Type", "application/xml");
            post.setHeader("Authorization", "Basic ZGVidWc6WG9vbmcxZWU=");

            entity = new ByteArrayEntity(xmlParser(stream).getBytes("UTF-8"));
            post.setEntity(entity);

            HttpResponse httpResponse = client.execute(post);
            System.out.println("Response Code : " + httpResponse.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            System.out.println(result);

            Long endTime = new Date().getTime();

            System.out.println("Processing time : " + (endTime - startTime) + " milliseconds");
            Thread.sleep(sleepTime);
        }
    }

    private static String xmlParser(InputStream stream) {
        try {
            InputStreamReader isReader = new InputStreamReader(stream,"UTF-8");
            BufferedReader br = new BufferedReader(isReader);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            return "";
        }
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public void setSleepTime(Long sleepTime) {
        this.sleepTime = sleepTime;
    }
}
