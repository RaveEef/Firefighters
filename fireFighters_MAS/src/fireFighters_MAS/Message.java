package fireFighters_MAS;
////NOTE ---> DO NOT MODIFY THIS FILE <---
/**
 * A class describing the Message instance
 * @author Kirill Tumanov, 2015-2017
 */
public class Message
{
	// Local variables declaration
	private String content; // A string containing the message passed
	/**
	 * Get the content from the message
	 * @return - the content
	 */
	public String getContent() { return content; }
	/**
	 * Set the content to the message
	 * @param k - the content
	 */
	public void setContent(String content) { this.content = content; }
	/**
	 * Get the cost of the message
	 * @return - the cost
	 */
	public int getCost() { return content.length(); }
}
