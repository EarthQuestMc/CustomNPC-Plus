package noppes.npcs;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.*;
import net.minecraft.util.StringUtils;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataSkinOverlays;
import noppes.npcs.util.ValueUtil;

import java.util.*;

public class DataDisplay {

    public EntityNPCInterface npc;

    public String name;
    public String title = "";

    private int markovGeneratorId = 8;
    private int markovGender = 0;

    /** 0 = normal, 1 = player */
    public byte skinType = 0;

    public String url = "";
    public String texture = "customnpcs:textures/entity/humanmale/Steve.png";
    public String cloakTexture = "";
    public String glowTexture = "";

    /** UTILISÉ PAR L’UI PLAYER */
    public GameProfile playerProfile;

    public DataSkinOverlays skinOverlayData;
    public AnimationData animationData;
    public HitboxData hitboxData;
    public TintData tintData;

    public int visible = 0;
    public int modelSize = 5;
    public int showName = 0;
    public int modelType = 0;
    public long overlayRenderTicks = 0;

    public boolean disableLivingAnimation = false;
    public byte showBossBar = 0;

    @SideOnly(Side.CLIENT)
    public boolean isInvisibleToMe;

    public ArrayList<UUID> invisibleToList = new ArrayList<>();

    @SideOnly(Side.CLIENT)
    public HashSet<Integer> tempInvisIds;

    public DataDisplay(EntityNPCInterface npc) {
        this.npc = npc;
        skinOverlayData = new DataSkinOverlays(npc);
        name = getRandomName();
        animationData = new AnimationData(this);
        hitboxData = new HitboxData();
        tintData = new TintData();
    }

    /* ================= API OBLIGATOIRE ================= */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (Objects.equals(this.name, name))
            return;
        this.name = name;
        npc.updateClient = true;
    }

    public int getMarkovGeneratorId() {
        return markovGeneratorId;
    }

    public int getMarkovGender() {
        return markovGender;
    }

    public void setMarkovGeneratorId(int id) {
        markovGeneratorId = ValueUtil.clamp(id, 0, CustomNpcs.MARKOV_GENERATOR.length - 1);
    }

    public void setMarkovGender(int gender) {
        markovGender = ValueUtil.clamp(gender, 0, 2);
    }

    public String getRandomName() {
        return CustomNpcs.MARKOV_GENERATOR[markovGeneratorId].fetch(markovGender);
    }

    /**
     * ⚠️ MÉTHODE CLÉ
     * CustomNPC-Plus l’utilise pour décider de la texture
     */
    @SideOnly(Side.CLIENT)
    public String getSkinTexture() {
        return texture;
    }


    public void setSkinTexture(String texture) {
        if (Objects.equals(this.texture, texture))
            return;

        this.texture = texture;
        this.skinType = 0;

        npc.textureLocation = null;
        npc.updateClient = true;
    }

    /**
     * Appelée après édition via l’UI Player
     * → on invalide le cache
     */
    public void loadProfile() {
        npc.textureLocation = null;
        npc.updateClient = true;
    }

    /* ================= NBT ================= */

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

        nbt.setString("Name", name);
        nbt.setString("Title", title);

        nbt.setInteger("MarkovGeneratorId", markovGeneratorId);
        nbt.setInteger("MarkovGender", markovGender);

        nbt.setByte("UsingSkinUrl", skinType);
        nbt.setString("SkinUrl", url);
        nbt.setString("Texture", texture);

        if (playerProfile != null) {
            NBTTagCompound prof = new NBTTagCompound();
            NBTUtil.func_152460_a(prof, playerProfile);
            nbt.setTag("SkinUsername", prof);
        }

        nbt = skinOverlayData.writeToNBT(nbt);
        nbt = animationData.writeToNBT(nbt);
        nbt = hitboxData.writeToNBT(nbt);
        nbt = tintData.writeToNBT(nbt);

        nbt.setInteger("Size", modelSize);
        nbt.setInteger("modelType", modelType);
        nbt.setInteger("ShowName", showName);
        nbt.setInteger("NpcVisible", visible);

        nbt.setBoolean("NoLivingAnimation", disableLivingAnimation);
        nbt.setByte("BossBar", showBossBar);

        NBTTagList invis = new NBTTagList();
        for (UUID id : invisibleToList) {
            invis.appendTag(new NBTTagString(id.toString()));
        }
        nbt.setTag("InvisibleToList", invis);

        return nbt;
    }

    public void readToNBT(NBTTagCompound nbt) {

        setName(nbt.getString("Name"));
        title = nbt.getString("Title");

        markovGeneratorId = nbt.getInteger("MarkovGeneratorId");
        markovGender = nbt.getInteger("MarkovGender");

        skinType = nbt.getByte("UsingSkinUrl");
        url = nbt.getString("SkinUrl");
        texture = nbt.getString("Texture");

        if (nbt.hasKey("SkinUsername", 10)) {
            playerProfile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkinUsername"));
        }

        skinOverlayData.readFromNBT(nbt);
        animationData.readFromNBT(nbt);
        hitboxData.readFromNBT(nbt);
        tintData.readFromNBT(nbt);

        modelSize = ValueUtil.clamp(nbt.getInteger("Size"), 1, ConfigMain.NpcSizeLimit);
        modelType = nbt.getInteger("modelType");
        showName = nbt.getInteger("ShowName");
        visible = nbt.getInteger("NpcVisible");

        disableLivingAnimation = nbt.getBoolean("NoLivingAnimation");
        showBossBar = nbt.getByte("BossBar");

        invisibleToList.clear();
        NBTTagList list = nbt.getTagList("InvisibleToList", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            invisibleToList.add(UUID.fromString(list.getStringTagAt(i)));
        }

        npc.textureLocation = null;
        npc.updateHitbox();
    }

    public boolean showName() {
        if (npc.isKilled())
            return false;
        return showName == 0 || (showName == 2 && npc.isAttacking());
    }

    @SideOnly(Side.CLIENT)
    public boolean getTempScriptInvisible(int entityId) {
        if (tempInvisIds == null) {
            tempInvisIds = new HashSet<>();
        }
        return tempInvisIds.contains(entityId);
    }
}
