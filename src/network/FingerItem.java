package network;

public class FingerItem {

	ChordKey start;
	IndexNode node;

	public FingerItem(ChordKey start, IndexNode node) {
		this.node = node;
		this.start = start;
	}

	public ChordKey getStart() {
		return start;
	}

	public void setStart(ChordKey start) {
		this.start = start;
	}

	public IndexNode getNode() {
		return node;
	}

	public void setNode(IndexNode node) {
		this.node = node;
	}

}