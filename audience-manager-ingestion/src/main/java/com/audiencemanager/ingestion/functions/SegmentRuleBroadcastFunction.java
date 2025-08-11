package com.audiencemanager.ingestion.functions;

import com.audiencemanager.ingestion.model.SegmentRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Source function that periodically fetches segment rules from the API and broadcasts them
 */
public class SegmentRuleBroadcastFunction implements SourceFunction<SegmentRule> {
    
    private static final Logger LOG = LoggerFactory.getLogger(SegmentRuleBroadcastFunction.class);
    private static final long serialVersionUID = 1L;
    
    private final String apiBaseUrl;
    private volatile boolean isRunning = true;
    private transient ObjectMapper objectMapper;
    private transient CloseableHttpClient httpClient;
    
    public SegmentRuleBroadcastFunction(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
    
    @Override
    public void run(SourceContext<SegmentRule> ctx) throws Exception {
        objectMapper = new ObjectMapper();
        httpClient = HttpClients.createDefault();
        
        LOG.info("Starting segment rules broadcast from API: {}", apiBaseUrl);
        
        while (isRunning) {
            try {
                List<SegmentRule> rules = fetchSegmentRules();
                LOG.info("Fetched {} segment rules from API", rules.size());
                
                for (SegmentRule rule : rules) {
                    synchronized (ctx.getCheckpointLock()) {
                        ctx.collect(rule);
                    }
                }
                
                // Wait for 30 seconds before next fetch
                Thread.sleep(30000);
                
            } catch (Exception e) {
                LOG.error("Failed to fetch segment rules from API", e);
                // Wait before retrying
                Thread.sleep(10000);
            }
        }
    }
    
    private List<SegmentRule> fetchSegmentRules() throws Exception {
        String url = apiBaseUrl + "/segments/rules"; // Assuming we'll add this endpoint
        HttpGet request = new HttpGet(url);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (response.getCode() != 200) {
                throw new RuntimeException("Failed to fetch rules: " + response.getCode() + " - " + responseBody);
            }
            
            LOG.debug("API response: {}", responseBody);
            return objectMapper.readValue(responseBody, new TypeReference<List<SegmentRule>>() {});
        }
    }
    
    @Override
    public void cancel() {
        isRunning = false;
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                LOG.warn("Failed to close HTTP client", e);
            }
        }
    }
}