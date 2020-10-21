import java.util.List;
import java.util.Map;

public class Sample {

	public static void printMap(Map<String, Object> map) {
		for (Map.Entry<String, Object> p : map.entrySet()) {
			System.out.printf("\t\t %-15s \t:\t %s\n", p.getKey(), p.getValue());
		}
		System.out.println("\n");
	}

	public static void main(String args[]) {

		String username = "<username>";
		String password = "<password>";
		String ticket = "<ticket>";

		// Jira Details
		Map<String, Object> ticketDetails = JiraRestClient.getClient(username, password).getJiraDetails(ticket);
		System.out.println("Ticket Details :");
		printMap(ticketDetails);

		// Jira Comments
		List<Map<String, Object>> comments = JiraRestClient.getClient(username, password).getComments(ticket);
		System.out.println("Comments :");
		for (int index = 0; index < comments.size(); index++) {
			System.out.printf("\tComment #%d\n", index);
			printMap(comments.get(index));
		}

		// Jira Changelog
		List<Map<String, Object>> changelog = JiraRestClient.getClient(username, password).getChangelog(ticket);
		System.out.println("Changelog :");
		for (int index = 0; index < changelog.size(); index++) {
			System.out.printf("\tChangelog #%d\n", index);
			printMap(changelog.get(index));
		}
	}

}
