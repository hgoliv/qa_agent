package pt.uc.dei.qa.answers;

import org.apache.lucene.document.Document;

import pt.uc.dei.qa.agents.AbstractAgent;

public class Hit {

	private String service;
	private String question;
	private String answer;
	
	public Hit(Document doc) {
		
		this.service = doc.getField(AbstractAgent.SERVICE_FIELD).stringValue();
		this.question = doc.getField(AbstractAgent.QUESTION_FIELD).stringValue();
		this.answer = doc.getField(AbstractAgent.ANSWER_FIELD).stringValue();
	}

	public String getService() {
		return service;
	}

	public String getQuestion() {
		return question;
	}

	public String getAnswer() {
		return answer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
		result = prime * result + ((question == null) ? 0 : question.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hit other = (Hit) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}
}
