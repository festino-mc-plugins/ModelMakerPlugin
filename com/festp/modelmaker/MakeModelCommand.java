package com.festp.modelmaker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MakeModelCommand implements CommandExecutor {
	public static final String SEPARATOR = System.getProperty("file.separator");
	
	public String getCommand() {
		return "makemodel";
	}
	
	/** @return min x, min y, size */
	private Vector getColorPlace(int i) {
		double size = 8;
		int length = 1;
		int layerLength = 2 * length - 1;
		while (i >= layerLength) {
			i -= layerLength;
			size /= 2;
			length = 2 * length + 1;
			layerLength = 2 * length - 1;
		}
		if (i < length)
			return new Vector(i * size, (length - 1) * size, size);
		return new Vector((length - 1) * size, (layerLength - i - 1) * size, size);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		// /makemodel name center{x,y,z} offsets{+-x/2, +y, +-z/2} scale{factor}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Playern't.");
			return false;
		}
		
		World world = ((Player) sender).getWorld();
		
		try {
			String name = args[0].toLowerCase();
			
			double xCenter = Double.parseDouble(args[1]);
			double yMin = Double.parseDouble(args[2]);
			double zCenter = Double.parseDouble(args[3]);

			double xOffset = Double.parseDouble(args[4]);
			double yOffset = Double.parseDouble(args[5]);
			double zOffset = Double.parseDouble(args[6]);
			
			double scale = Double.parseDouble(args[7]);

			double xMin = - xOffset / 2;
			double zMin = - zOffset / 2;

			int materialIndex = 0;
			List<Material> materials = new ArrayList<>();
			List<String> materialUV = new ArrayList<>();
			String elements = "\"elements\": [";
			int elementCount = 0;
			
			for (int x = (int) xMin; x < xMin + xOffset; x++) {
				for (int z = (int) zMin; z < zMin + zOffset; z++) {
					double xCur = 8 + x * scale;
					double zCur = 8 + z * scale;
					int yTop = (int) yOffset;
					Material lastMaterial = Material.AIR;
					Material material = Material.AIR;
					for (int y = yTop - 1; y >= -1; y--) {
						if ((int)yMin + y >= 0)
							material = world.getBlockAt((int)xCenter + x, (int)yMin + y, (int)zCenter + z).getType();
						if (material != lastMaterial || y < 0) {
							if (lastMaterial != Material.AIR) {
								elementCount++;
								String nameTag = "\"name\": \"" + elementCount + "\"";
								double xFrom = xCur;
								double zFrom = zCur;
								double xTo = xFrom + scale;
								double zTo = zFrom + scale;
								double yFrom = (y + 1) * scale;
								double yTo = yTop * scale;
								String from = "\"from\": [ " + xFrom + ", " + yFrom + ", " + zFrom + " ]";
								String to = "\"to\": [ " + xTo + ", " + yTo + ", " + zTo + " ]";
								String textureTag = "{ \"texture\": \"#tex\", \"uv\": " + materialUV.get(materialIndex) + " }";
								String element = "\t\t" + nameTag + ",\n\t\t" + from + ",\n\t\t" + to + ",\n\t\t";
								element += "\"faces\": { \"north\": " + textureTag + ", \"east\": " + textureTag + ", \"south\": " + textureTag
										+ ", \"west\": " + textureTag + ", \"up\": " + textureTag + ", \"down\": " + textureTag + "\t}";
								element = "\n\t{\n" + element + "\n\t}";
								
								if (elementCount > 1)
									elements += ",";
								elements += element;
							}
							if (y < 0)
								break;
							
							lastMaterial = material;
							yTop = y + 1;
							if (lastMaterial != Material.AIR) {
								boolean found = false;
								for (int i = 0; i < materials.size(); i++) {
									if (materials.get(i) == lastMaterial) {
										materialIndex = i;
										found = true;
										break;
									}
								}
								if (!found) {
									materialIndex = materials.size();
									materials.add(lastMaterial);
									Vector texCoord = getColorPlace(materialIndex);
									String strUV = "[ " + texCoord.getX() + ", " + texCoord.getY() + ", "
												+ (texCoord.getX() + texCoord.getZ()) + ", " + (texCoord.getY() + texCoord.getZ()) + " ]";
									materialUV.add(strUV);
								}
							}
						}
					}
				}
			}

			sender.sendMessage(ChatColor.GREEN + "Resourcepack needs assets/minecraft/textures/block/" + name + ".png");
			for (int i = 0; i < materials.size(); i++) {
				Material m = materials.get(i);
				String matName = m.toString().toLowerCase();
				sender.sendMessage(ChatColor.GREEN + "You need pixel " + materialUV.get(i) + " for " + matName + " :D");
				
			}

			String header = "\"textures\": { ";
			header += "\n\t\"tex\": \"minecraft:block/" + name + "\"";
			header += "\n }";
			
			elements += "\n ]";
			
			String display = "";

			String dirPath = "plugins" + SEPARATOR + "ModelMaker";
			String fileName = dirPath + SEPARATOR + name + ".json";
			File file = new File(fileName);
			try {
				File path = new File(dirPath);
				path.mkdirs();
				file.createNewFile();
				FileWriter writer = new FileWriter(file, false);
				writer.append("{\n");
				writer.append(header);
				writer.append(",\n");
				writer.append(display);
				writer.append("\n");
				writer.append(elements);
				writer.append("\n}");
				writer.close();
				sender.sendMessage(ChatColor.GREEN + "Search your brand new model in " + ChatColor.UNDERLINE + fileName
						+ ChatColor.RESET + ChatColor.GREEN + "!");
			} catch (IOException ex) {
				sender.sendMessage(ChatColor.RED + "Can't IO...\n" + ex.getMessage());
			}
		} catch(Exception ex) {
			sender.sendMessage(ChatColor.RED + "Rubbish!\n" + ex.getMessage());
		}
		return true;
	}
}
