package network;

public class FingerTable {
	FingerItem[] fingers;
	
	public FingerTable(IndexNode node) {
		this.fingers = new FingerItem[Hash.KEY_LENGTH];
		for (int i = 0; i < fingers.length; i++) {
			ChordKey start = node.getNodeID().createStartKey(i);
			fingers[i] = new FingerItem(start, node);
		}
	}

	public FingerItem getFinger(int i) {
		return fingers[i];
	}

}
