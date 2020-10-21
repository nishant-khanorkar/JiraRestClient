import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Rest client for Jira
 * 
 */
public class JiraRestClient {

	public JiraRestClient(String username, String password) {
		this.username = username;
		this.password = password;
	}

	private String username;
	private String password;

	private final static String REST_SERVICE_URI = "https://<site-url>/rest/api/latest/";
	private final static String JIRA_URL = String.format("%sissue/", REST_SERVICE_URI);

	private static HashMap<Long, JiraRestClient> restClientMap = new HashMap<Long, JiraRestClient>();

	public static JiraRestClient getClient(String username, String password) {
		if (!restClientMap.containsKey(Thread.currentThread().getId())) {
			restClientMap.put(Thread.currentThread().getId(), new JiraRestClient(username, password));
		}
		return restClientMap.get(Thread.currentThread().getId());
	}

	/*
	 * Prepare HTTP Headers.
	 */
	private HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}

	/*
	 * Add HTTP Authorization header, using Basic-Authentication to send
	 * client-credentials.
	 */
	private HttpHeaders getHeadersWithClientCredentials() {
		String plainClientCredentials = String.format("%s:%s", username, password);
		String base64ClientCredentials = new String(Base64.encodeBase64(plainClientCredentials.getBytes()));

		HttpHeaders headers = getHeaders();
		headers.add("Authorization", "Basic " + base64ClientCredentials);
		return headers;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getJiraDetails(String jiraNumber) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = new HttpEntity<String>(getHeadersWithClientCredentials());
		try {
			ResponseEntity<Object> response = restTemplate.exchange(String.format(
					"%s%s?fields=summary,description,reporter,assignee,status,issuetype,priority,created,updated",
					JIRA_URL, jiraNumber), HttpMethod.GET, request, Object.class);
			LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) response.getBody();
			if (responseMap != null) {
				LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) responseMap.get("fields");
				map.put("id", responseMap.get("key"));
				return map;
			}
		} catch (Exception e) {
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getComments(String jiraNumber) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = new HttpEntity<String>(getHeadersWithClientCredentials());
		try {
			ResponseEntity<Object> response = restTemplate.exchange(String.format("%s%s/comment", JIRA_URL, jiraNumber),
					HttpMethod.GET, request, Object.class);
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) response.getBody();
			if (map != null) {
				ArrayList<?> data = (ArrayList<?>) map.get("comments");
				return (List<Map<String, Object>>) data;
			}
		} catch (Exception e) {
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getChangelog(String jiraNumber) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = new HttpEntity<String>(getHeadersWithClientCredentials());
		try {
			ResponseEntity<Object> response = restTemplate.exchange(
					String.format("%s%s?fields=summary&expand=changelog", JIRA_URL, jiraNumber), HttpMethod.GET,
					request, Object.class);
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) response.getBody();
			if (map != null) {
				ArrayList<?> data = (ArrayList<?>) ((LinkedHashMap<String, Object>) map.get("changelog"))
						.get("histories");
				return (List<Map<String, Object>>) data;
			}
		} catch (Exception e) {
		}
		return null;
	}
}