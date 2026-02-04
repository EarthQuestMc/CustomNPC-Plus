package noppes.npcs.client.gui.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.util.GuiEffectBar;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerEffect;
import org.lwjgl.input.Mouse;

public class GuiCNPCInventory extends GuiNPCInterface {
    public static final ResourceLocation specialIcons = new ResourceLocation("customnpcs", "textures/gui/icons.png");

    public static int activeTab = -100;
    protected Minecraft mc = Minecraft.getMinecraft();
    private GuiEffectBar effectBar;

    public GuiCNPCInventory() {
        super();
        xSize = 280;
        ySize = 180;
        drawDefaultBackground = false;
    }

    public void initGui() {
        super.initGui();
        guiTop += 10;

        int y = 3;
        GuiMenuSideButton questsButton = new GuiMenuSideButton(-100, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
        questsButton.rightSided = true;
        questsButton.active = activeTab == -100;
        questsButton.renderIconPosX = 32;
        questsButton.renderResource = specialIcons;
        addSideButton(questsButton);

        int effectBarX = guiLeft - 40;
        int effectBarY = guiTop + 10;
        int effectBarWidth = 28;
        int effectBarHeight = ySize;
        effectBar = new GuiEffectBar(effectBarX, effectBarY, effectBarWidth, effectBarHeight);
    }

    private void updateEffectBar() {
        if (effectBar != null) {
            effectBar.entries.clear();
            PlayerData data = PlayerData.get(mc.thePlayer);
            if (data != null && data.effectData != null) {
                for (PlayerEffect pe : data.effectData.getEffects().values()) {
                    // TODO: Pulls specific CustomEffect entries from the global controller; this HUD is part of the base
                    //       player inventory so it must work without extra permissions (no CustomNpcsPermissions required).
                    CustomEffect effect = CustomEffectController.getInstance().get(pe.id, pe.index);
                    if (effect != null) {
                        effectBar.entries.add(new GuiEffectBar.EffectEntry(effect, pe));
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateEffectBar();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!ConfigClient.HideEffectsBar && effectBar != null && !effectBar.entries.isEmpty()) {
            if (activeTab != -100)
                effectBar.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void handleMouseInput() {
        int delta = Mouse.getDWheel();
        if (delta != 0 && effectBar != null) {
            int mouseX = Mouse.getX() * this.width / mc.displayWidth;
            int mouseY = this.height - Mouse.getY() * this.height / mc.displayHeight - 1;
            if (mouseX >= effectBar.x && mouseX < effectBar.x + effectBar.width &&
                mouseY >= effectBar.y && mouseY < effectBar.y + effectBar.height) {
                // Use delta/120 to convert the typical scroll wheel values to ±1
                effectBar.mouseScrolled(delta / 120);
            }
        }
        super.handleMouseInput();
    }


    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id <= -100) {
            if (guibutton.id == -100 && activeTab != -100) {
                activeTab = -100;
                mc.displayGuiScreen(new GuiQuestLog());
            }
        }
    }

    @Override
    public void save() { }
}
