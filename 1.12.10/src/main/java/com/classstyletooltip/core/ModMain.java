package com.classstyletooltip.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmorStand;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemCarrotOnAStick;
import net.minecraft.item.ItemClock;
import net.minecraft.item.ItemCoal;
import net.minecraft.item.ItemCompass;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemEmptyMap;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFireworkCharge;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemHangingEntity;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemSaddle;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@Mod(modid = ModMain.MODID, version = ModMain.VERSION, clientSideOnly=true) //Only need to be client side on this.
public class ModMain
{
    public static final String MODID = "class_style_tooltip";
    public static final String VERSION = "1.0";
    public static boolean ReportUnassocatedClass=true;									//DEBUG: Output any classes not in lists.
    private static ClientCommandDebugULC ULC_cmd;										//A command to toggle debug mode : DBG_Toggle_ULC
    private static Map<String, String> Item_map_config = new HashMap<String, String>();	//Table of strings to look up item categories from config.
    private static Map<String, String> Item_map = new HashMap<String, String>();		//Table of strings to look up item categories from mods.
    private static Map<String, Method> Callbacks = new HashMap<String, Method>();		//Table of call backs to call for item cataloguing from mods.
    private static Configuration config;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	System.out.println("Loading class style prototype!");
    	
    	ModMain.RegistrationHandler hForgeRegistrationHandler=new ModMain.RegistrationHandler();
    	MinecraftForge.EVENT_BUS.register(hForgeRegistrationHandler);
    	
    	//API test(Works fine)
    	//FMLInterModComms.sendMessage(ModMain.MODID, "Add-Classification", "item.ingotGold@net.minecraft.item.Item=material");
    	
    	//Add default module for handling vanilla items.
    	FMLInterModComms.sendMessage(ModMain.MODID, "Add-ClassifyHandler", "com.classstyletooltip.core.ModMain@VanillaUnlocalizedHandler");

    	//Quick client side command hook
    	if (event.getSide() == Side.CLIENT)
    	{
    		if (ULC_cmd == null) { ULC_cmd = new ClientCommandDebugULC(); }
    		net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(ULC_cmd);
    	}
    }
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
    	System.out.println("Running class style preinit");
    	{
    		config = new Configuration(new File("config/" + ModMain.MODID + ".cfg"));
			config.load();
			Property RUC = config.get("Debug", "ReportUnassocatedClass", false);
			RUC.setComment("Used to find unhandled tooltips.");
			RUC.setShowInGui(true);
			ReportUnassocatedClass=RUC.getBoolean();
			
			config.setCategoryComment("UserData", "Define extra strings here if needed or if a override is wanted.");
			ConfigCategory userdata_cat=config.getCategory("UserData");
			userdata_cat.setShowInGui(false);
			userdata_cat.setRequiresMcRestart(true);
			for (Property prop : userdata_cat.values()) {
				//If our entry contains a equal then it is compatible.
				if (prop.getString().lastIndexOf("=") != -1)
				{
					String Message, Key, Value;
                	Message=prop.getString(); 												//Grab our string...
                	Key=Message.substring(0, Message.lastIndexOf("="));						//Get the key.
                	Value=Message.substring(Message.lastIndexOf("=")+1, Message.length());	//Get the value.
                	System.out.println("Pushing rule from config : "+ prop.getString());
					Item_map_config.put(Key, Value);
				}
			}
    	}
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    	System.out.println("Running class style postinit");
        if (config.hasChanged()) {
            config.save();
        }
    }
    
   	static public String VanillaUnlocalizedHandler(String item_ident, Item item_object)
   	{
   		String ret="";
   		switch (item_object.getUnlocalizedName())
		{
			case "item.diamond":
			case "item.ingotIron":
			case "item.ingotGold":
			case "item.stick":
			case "item.bowl":
			case "item.string":
			case "item.feather":
			case "item.sulphur":
			case "item.wheat":
			case "item.flint":
			case "item.leather":
			case "item.brick":
			case "item.clay":
			case "item.reeds":
			case "item.paper":
			case "item.book":
			case "item.slimeball":
			case "item.yellowDust":
			case "item.bone":
			case "item.sugar":
			case "item.blazeRod":
			case "item.goldNugget":
			case "item.ironNugget":
			case "item.glassBottle":
			case "item.emerald":
			case "item.netherStar":
			case "item.netherbrick":
			case "item.netherquartz":
			case "item.prismarineShard":
			case "item.prismarineCrystals":
			case "item.rabbitHide":
			case "item.shulkerShell":
			case "item.chorusFruitPopped":
				ret="Material"; break;
			case "item.ghastTear":
			case "item.fermentedSpiderEye":
			case "item.blazePowder":
			case "item.magmaCream":
			case "item.speckledMelon":
			case "item.rabbitFoot":
			case "item.dragon_breath":
				ret="Regent"; break;
			case "item.cake":
				ret="Edible,Placeable"; break;
			case "item.diode":
			case "item.brewingStand":
			case "item.cauldron":
			case "item.flowerPot":
			case "item.comparator":
				ret="Placeable"; break;
			case "item.minecartChest":
			case "item.minecartFurnace":
			case "item.minecartTnt":
			case "item.minecartHopper":
				ret="Vehicle"; break;
			case "item.horsearmormetal":
			case "item.horsearmorgold":
			case "item.horsearmordiamond":
				ret="Armor"; break; //Armor
			case "item.totem":
				ret="Held"; break;
			default: break;
		}
    		
   		return ret;
   	}
    
    //Won't capture the tooltip event so we register above.
    //@Mod.EventBusSubscriber(modid = ModMain.MODID)
    public class RegistrationHandler {
    	//Intercept tool tips here for simple dealings.
    	@SideOnly(Side.CLIENT)
    	@SubscribeEvent(priority = EventPriority.LOW)
    	public void onToolTip(ItemTooltipEvent event) {
    		int tokens=0;
    		String ItemUseFormat = "";
    		ItemStack itemStack = event.getItemStack();
    		List<String> toolTip = event.getToolTip();
    		
    		//Found an item stack..
    		if (itemStack != null)
    		{
    			Item sampleItem=itemStack.getItem();
    			if (sampleItem != null)
    			{
    				TextFormatting TextColor = TextFormatting.fromColorIndex(5); //DARK_PURPLE
    				if (TextColor != null) { ItemUseFormat += TextColor.toString(); }
    				String classification="";
    				if (sampleItem instanceof Item)
    				{
    					classification="Item";
    					
    					//Might make these initially assign a single token instead of +1 on the var.
    					//A few extra vanilla classes added to make list complete.
    					if (sampleItem instanceof ItemSkull) { classification="Placeable"; tokens+=1;  }
    					if (sampleItem instanceof ItemSign)  { classification="Placeable"; tokens+=1;  }
    					if (sampleItem instanceof ItemMinecart) 
    					{ 
    						//Reflect here to grab the type...
    						EntityMinecart.Type minecartType = ObfuscationReflectionHelper.getPrivateValue(ItemMinecart.class, (ItemMinecart)sampleItem, "minecartType", "field_77841_a");
    						if (minecartType == EntityMinecart.Type.RIDEABLE)
    						{
    							classification="Transport"; 
    							tokens+=1;
    						}
    					}
    					
    					if (sampleItem instanceof ItemBoat) { classification="Transport"; tokens+=1; }
    					if (sampleItem instanceof ItemFlintAndSteel) { classification="Tool"; tokens+=1; }
    					
    					//Generics
    					if (sampleItem.getItemUseAction(itemStack) == EnumAction.BLOCK) { classification="Defensive"; tokens+=1; }
    					if (sampleItem.getItemUseAction(itemStack) == EnumAction.EAT) { classification="Consumable"; tokens+=1; }
    					if (sampleItem.getItemUseAction(itemStack) == EnumAction.DRINK) 
    					{
    						classification="Drinkable"; tokens+=1;
    						if (sampleItem instanceof ItemLingeringPotion) { classification="Thrown"; }	//Fix up for lingering potion types.
    						if (sampleItem instanceof ItemSplashPotion) { classification="Thrown"; }	//Fix up for splash potion types.
    					}
    					
    					if (sampleItem.getItemUseAction(itemStack) == EnumAction.BOW) { classification="Shootable"; tokens+=1; }
    					
    					//Vanilla base class handling.
    					if (sampleItem instanceof ItemBlock)   			{ classification="Placeable"; 	tokens+=1; 	}
    					if (sampleItem instanceof ItemDoor)    			{ classification="Placeable"; 	tokens+=1; 	}
    					if (sampleItem instanceof ItemBed)	   			{ classification="Placeable"; 	tokens+=1; 	}
    					if (sampleItem instanceof ItemArmorStand) 		{ classification="Placeable"; 	tokens+=1; 	}
    					if (sampleItem instanceof ItemHangingEntity)	{ classification="Placeable";	tokens+=1;	}
    					if (sampleItem instanceof ItemArmor)   			{ classification="Armour"; 		tokens+=1;  }
    					if (sampleItem instanceof ItemBow)	   			{ classification="Bow"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemTool)    			{ classification="Tool"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemAxe)	   			{ classification="Axe"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemHoe)	   			{ classification="Hoe"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemSpade)   			{ classification="Spade"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemPickaxe) 			{ classification="Pickaxe"; 	tokens+=1; 	}
    					if (sampleItem instanceof ItemSword)   			{ classification="Sword"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemFishingRod)		{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemShield)  			{ classification="Shield"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemFood)    			{ classification="Edible"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemArrow)   			{ classification="Ammo"; 		tokens+=1; 	}
    					if (sampleItem instanceof ItemDye)				{ classification="Dye";			tokens+=1;	}
    					
    					if (sampleItem instanceof ItemBucket)			{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemLead)				{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemSaddle)			{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemCoal)				{ classification="Material";	tokens+=1;	}
    					if (sampleItem instanceof ItemRedstone)			{ classification="Material,Placeable";	tokens+=1;	} //Is there a better way to catalogue this?
    					if (sampleItem instanceof ItemSnowball)			{ classification="Thrown";		tokens+=1;	}
    					if (sampleItem instanceof ItemEgg)				{ classification="Thrown";		tokens+=1;	}
    					if (sampleItem instanceof ItemCompass)			{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemClock)			{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemShears)			{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemEnderPearl)		{ classification="Thrown";		tokens+=1;	}
    					if (sampleItem instanceof ItemEnderEye)			{ classification="Thrown";		tokens+=1;	}
    					if (sampleItem instanceof ItemExpBottle)		{ classification="Thrown";		tokens+=1;	}
    					if (sampleItem instanceof ItemFireball)			{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemWritableBook)		{ classification="Readable";	tokens+=1;	}
    					if (sampleItem instanceof ItemMapBase)			{ classification="Map";			tokens+=1;	}
    					if (sampleItem instanceof ItemEmptyMap)			{ classification="Material";	tokens+=1;	}
    					if (sampleItem instanceof ItemCarrotOnAStick)	{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemMonsterPlacer)	{ classification="Spawnable";	tokens+=1;	}
    					if (sampleItem instanceof ItemRecord)			{ classification="Music";		tokens+=1;	}
    					if (sampleItem instanceof ItemNameTag)			{ classification="Tool";		tokens+=1;	}
    					if (sampleItem instanceof ItemElytra)			{ classification="Flight";		tokens+=1;	}
    					if (sampleItem instanceof ItemEndCrystal)		{ classification="Placeable";	tokens+=1;	}
    					if (sampleItem instanceof ItemFireworkCharge)	{ classification="Thrown";		tokens+=1;	}
    					
    					if (sampleItem instanceof ItemEnchantedBook)
    					{
    						//What item can this enchantment be applyied to?
    						classification="Upgrade";
    						tokens+=1;
    					}
    					
    					if (sampleItem instanceof net.minecraftforge.common.IPlantable) 
    					{
    						net.minecraftforge.common.IPlantable plantable=(net.minecraftforge.common.IPlantable)sampleItem;
    						classification="Plantable";
    						
    						if (sampleItem instanceof ItemSeeds)
    						{
    							//Reflect here to grab the info...
    							//"soilBlockID" -> "field_77838_b"
    							Block Soil = ObfuscationReflectionHelper.getPrivateValue(ItemSeeds.class, (ItemSeeds)plantable, "soilBlockID", "field_77838_b");
    							if (Soil != null)
    							{
    								classification += "(In " + Soil.getLocalizedName() + ")";
    							}
    						}
    						tokens+=1;
    					} //Check what soil this can go in?
    					
    					//API/callback override.
    					for (Method value : Callbacks.values()) {
    					    // Cycle through call backs and see if they know what this item should be grouped as.
    						try {
								String result = (String)value.invoke(null, sampleItem.getUnlocalizedName() + "@" + sampleItem.getClass().getName(), sampleItem);
								if (!result.isEmpty())
								{
									classification=result;
									tokens+=1;
								}
							} 
    						catch (IllegalAccessException e) 
    						{ System.out.println("Instance creation failed.");		}
    						catch (IllegalArgumentException e) 
    						{ System.out.println("Function signature incorrect."); 	} 
    						catch (InvocationTargetException e) 
    						{ System.out.println("Invocation target Exception."); 	}
    					}
    					
    					//API/mapping table override.
    					if (Item_map.get(sampleItem.getUnlocalizedName() + "@" + sampleItem.getClass().getName()) != null)
    					{
    						classification=Item_map.get(sampleItem.getUnlocalizedName() + "@" + sampleItem.getClass().getName());
    						tokens+=1;
    					}
    					
    					//If we have a definition in our config we will use that instead.
    					if (Item_map_config.get(sampleItem.getUnlocalizedName() + "@" + sampleItem.getClass().getName()) != null)
    					{
    						classification=Item_map_config.get(sampleItem.getUnlocalizedName() + "@" + sampleItem.getClass().getName());
    						tokens+=1;
    					}
    					
    					//Debug: output unassigned items by their class identity.
    					if (tokens == 0)
    					{
    						if (ReportUnassocatedClass)
    							System.out.println("!!Unassigned class : " + sampleItem.getUnlocalizedName() + "@" + sampleItem.getClass().getName());
    					}
    					
    					ItemUseFormat+=classification; 
    				}
    				
    				if (tokens > 0) { toolTip.add(ItemUseFormat); }
    			}
    		}
    	}
    }
    
    //IMC handler for pushing classification of an item to the stack
    //ref: http://minecraftforgetuts.weebly.com/inter-mod-communication.html
    //
    //Interface 
    //FMLInterModComms.sendMessage("class_style_tooltip", "Add-Classification", "Device@MachineClass=Machine");
    //or
    //FMLInterModComms.sendMessage("class_style_tooltip", "Add-ClassifyHandler", "class@Method");
    //
    //The prototype will be assumed to take <String>, <Item> and return a <String> and also be static.
    //E.x static public String Callback(String item_ident, Item item_object);
    //
    @EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event)
    {
        for (final FMLInterModComms.IMCMessage imcMessage : event.getMessages())
        {
            if (imcMessage.key.equalsIgnoreCase("Add-Classification"))
            {
                if (imcMessage.isStringMessage())
                {
                	String Message, Key, Value;
                	Message=imcMessage.getStringValue(); 									//Grab our string...
                	Key=Message.substring(0, Message.lastIndexOf("="));						//Get the key.
                	Value=Message.substring(Message.lastIndexOf("=")+1, Message.length());	//Get the value.
                	
                	//Self test for api
                	//System.out.println("KV= " + Key + "=" + Value);
                	System.out.println("Register : " + Key);
                	Item_map.put(Key, Value);
                }
            }
            if (imcMessage.key.equalsIgnoreCase("Add-ClassifyHandler"))
            {
            	String Message, Key, Value;
            	Message=imcMessage.getStringValue(); 									//Grab our string...
            	Key=Message.substring(0, Message.lastIndexOf("@"));						//Get the key.
            	Value=Message.substring(Message.lastIndexOf("@")+1, Message.length());	//Get the value.
            	
            	try {
					Class cls=Class.forName(Key);
					Method multiplyStaticMethod=cls.getDeclaredMethod(Value, String.class, Item.class);
					
					//Push it to the callback map using the class as a key.
					System.out.println("Register : " + Key + "@" + Value);
					Callbacks.put(Key, multiplyStaticMethod);
				} 
            	catch (ClassNotFoundException e)
            	{ System.out.println("No class found : " + Key); } //Did not find the class.
            	catch (NoSuchMethodException | SecurityException e) 
            	{ System.out.println("Can not access : " + Value); } //Did not find method or java's security kicked off.
            }
        }
    }
}
