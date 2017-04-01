package minecraftflightsimulator;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = MFS.MODID, name = MFS.MODNAME, version = MFS.MODVER)
public class MFS {
	public static final String MODID="mfs";
	public static final String MODNAME="Minecraft Flight Simulator";
	public static final String MODVER="8.0.0-ALPHA-5";
	
	@Instance(value = MFS.MODID)
	public static MFS instance;
	public static final SimpleNetworkWrapper MFSNet = NetworkRegistry.INSTANCE.newSimpleChannel("MFSNet");
	@SidedProxy(clientSide="minecraftflightsimulator.ClientProxy", serverSide="minecraftflightsimulator.CommonProxy")
	public static CommonProxy proxy;
	
	/*INS194
	public MFS(){
		FluidRegistry.enableUniversalBucket();
	}
	INS194*/
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event){
		proxy.preInit(event);
		this.initModMetadata(event);
	}
	
	@EventHandler
	public void Init(FMLInitializationEvent event){
		proxy.init(event);
	}
	
	private void initModMetadata(FMLPreInitializationEvent event){
        ModMetadata meta = event.getModMetadata();
        meta.name = "Minecraft Flight Simulator";
        meta.description = "Realistic planes for Minecraft!";
        meta.authorList.clear();
        meta.authorList.add("don_bruce & CO.");
        meta.logoFile = "Vingette.png";
        meta.url = "http://minecraft.curseforge.com/projects/minecraft-flight-simulator";
        
        meta.modId = this.MODID;
        meta.version = this.MODVER;
        meta.autogenerated = false;
	}
}

