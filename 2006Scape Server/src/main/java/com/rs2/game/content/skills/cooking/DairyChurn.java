package com.rs2.game.content.skills.cooking;

import java.util.HashMap;

import com.rs2.GameConstants;
import com.rs2.event.CycleEvent;
import com.rs2.event.CycleEventContainer;
import com.rs2.event.CycleEventHandler;
import com.rs2.game.items.ItemAssistant;
import com.rs2.game.players.Player;
/**
 * Created by IntelliJ IDEA. User: vayken Date: 23/12/11 Time: 20:42 To change
 * this template use File | Settings | File Templates.
 */
public class DairyChurn {
	private static final int CHURN_ANIMATION = 894;

	public static enum ChurnData {
		CREAM(59238, new int[] { 1927 }, 2130, 21, 18), BUTTER(59239,
				new int[] { 1927, 2130 }, 6697, 38, 40), CHEESE(59240,
				new int[] { 1927, 2130, 6697 }, 1985, 48, 64);

		private int buttonId;
		private int[] used;
		private int result;
		private int level;
		private double experience;

		public static HashMap<Integer, ChurnData> churnItems = new HashMap<Integer, ChurnData>();

		public static ChurnData forId(int id) {
			return churnItems.get(id);
		}

		static {
			for (ChurnData data : ChurnData.values()) {
				churnItems.put(data.buttonId, data);
			}
		}

		private ChurnData(int buttonId, int[] used, int result, int level,
				double experience) {
			this.buttonId = buttonId;
			this.used = used;
			this.result = result;
			this.level = level;
			this.experience = experience;
		}

		public int getButtonId() {
			return buttonId;
		}

		public int[] getUsed() {
			return used;
		}

		public int getResult() {
			return result;
		}

		public int getLevel() {
			return level;
		}

		public double getExperience() {
			return experience;
		}

	}

	public static void churnItem(final Player player, int buttonId) {
		final ChurnData churnData = ChurnData.forId(buttonId);
		if (churnData == null || player.getStatedInterface() != "dairyChurn") {
			return;
		}
		if (player.playerLevel[GameConstants.COOKING] < churnData.getLevel()) {
			player.getDialogueHandler().sendStatement("You need a cooking level of " + churnData.getLevel() + " to make this.");
			return;
		}

		player.playerIsCooking = true;
		player.getPacketSender().closeAllWindows();
		
		CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
			@Override
			public void execute(CycleEventContainer container) {
				for (int i = 0; i <= churnData.getUsed().length - 1; i++) {
					if (!player.getItemAssistant().playerHasItem(churnData.getUsed()[i])) {
						player.getDialogueHandler().sendStatement("You don't have the required items to use the churn.");
						container.stop();
						return;
					}
				}
				if (!player.playerIsCooking) {
					container.stop();
					return;
				}
				container.setTick(5);
				player.startAnimation(CHURN_ANIMATION);
				player.getPacketSender().sendMessage("You make a " + ItemAssistant.getItemName(churnData.getResult()).toLowerCase() + ".");
				for (int i = 0; i < churnData.getUsed().length; i++)
					player.getItemAssistant().deleteItem(churnData.getUsed()[i], 1);
				player.getItemAssistant().addItem(churnData.getResult(), 1);
				player.getItemAssistant().addItem(1925, 1);
				player.getPlayerAssistant().addSkillXP(churnData.getExperience(), GameConstants.COOKING);
			}

			@Override
			public void stop() {
				player.getPlayerAssistant().resetAnimation();
			}
		}, 1);
	}
}
