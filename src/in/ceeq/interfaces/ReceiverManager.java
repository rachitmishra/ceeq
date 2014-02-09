package in.ceeq.interfaces;

import in.ceeq.actions.Receiver.ReceiverType;

public interface ReceiverManager {
	public void register(ReceiverType var);

	public void unregister(ReceiverType var);
}
