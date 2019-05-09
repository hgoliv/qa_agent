package pt.uc.dei.qa.answers;

public class HitScore implements Comparable<HitScore>{

	private float score;
	private Hit hit;
	
	public HitScore(Hit hit, float score) {
		this.hit = hit;
		this.score = score;
	}

	public float getScore() {
		return score;
	}

	public Hit getHit() {
		return hit;
	}

	@Override
	public int compareTo(HitScore other) {
		
		if(this.score < other.score)
			return 1;
		else if(this.score > other.score)
			return -1;
		
		return 0;
	}
	
}
