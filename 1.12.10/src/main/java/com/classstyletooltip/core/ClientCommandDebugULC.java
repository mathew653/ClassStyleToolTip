package com.classstyletooltip.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class ClientCommandDebugULC implements ICommand {
	private static List<String> aliases;
	static {
		aliases = new ArrayList<String>();
	}

	@Override
	public int compareTo(ICommand o) { return 0; }

	@Override
	public String getName() { return "DBG_Toggle_ULC"; }

	@Override
	public String getUsage(ICommandSender sender) { return "DBG_Toggle_ULC"; }

	@Override
	public List<String> getAliases() { return aliases; }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (ModMain.ReportUnassocatedClass == true) { ModMain.ReportUnassocatedClass=false; sender.sendMessage(new TextComponentString("[ClassStyleTooltip]Disabled debug")); }
		else										{ ModMain.ReportUnassocatedClass=true;	sender.sendMessage(new TextComponentString("[ClassStyleTooltip]Enabled debug")); }
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) { return true; }

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,	BlockPos targetPos) { return null; }

	@Override
	public boolean isUsernameIndex(String[] args, int index) { return false; }

}
