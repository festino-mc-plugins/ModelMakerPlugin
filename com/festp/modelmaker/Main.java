package com.festp.modelmaker;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin  {

	public void onEnable() {
		MakeModelCommand mainCommand = new MakeModelCommand();
		getCommand(mainCommand.getCommand()).setExecutor(mainCommand);
	}
}
