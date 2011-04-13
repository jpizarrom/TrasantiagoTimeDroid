package org.opensatnav.android.services.routing;

public class RouteInstructionPrompt {
	public int metresFromInstruction;
	public boolean basBeenSaid = false;
	
	public RouteInstructionPrompt(int metresFromInstruction) {
		this.metresFromInstruction = metresFromInstruction;
	}
}
