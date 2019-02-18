package org.czyee.guarder.node;

public class PermNode extends TreeNode {

	private int checked = 0;
	private int noPerm = 0;
	public void setChecked(int checked) {
		this.checked = checked;
	}

	public int getChecked() {
		return checked;
	}

	public void setNoPerm(int noPerm) {
		this.noPerm = noPerm;
	}

	public int getNoPerm() {
		return noPerm;
	}
}
