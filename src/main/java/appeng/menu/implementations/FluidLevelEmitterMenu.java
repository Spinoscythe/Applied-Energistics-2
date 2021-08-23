/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.parts.automation.FluidLevelEmitterPart;
import appeng.util.fluid.IAEFluidTank;

public class FluidLevelEmitterMenu extends FluidConfigurableMenu {

    public static final MenuType<FluidLevelEmitterMenu> TYPE = MenuTypeBuilder
            .create(FluidLevelEmitterMenu::new, FluidLevelEmitterPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData((host, buffer) -> {
                buffer.writeVarLong(host.getReportingValue());
            }, (host, menu, buffer) -> {
                menu.reportingValue = buffer.readVarLong();
            })
            .build("fluid_level_emitter");

    private final FluidLevelEmitterPart lvlEmitter;

    // Only synced once on menu-open, and only used on client
    private long reportingValue;

    public FluidLevelEmitterMenu(int id, final Inventory ip, final FluidLevelEmitterPart te) {
        super(TYPE, id, ip, te);
        this.lvlEmitter = te;
    }

    public long getReportingValue() {
        return reportingValue;
    }

    public void setReportingValue(long reportingValue) {
        if (isClient()) {
            if (reportingValue != this.reportingValue) {
                this.reportingValue = reportingValue;
                NetworkHandler.instance()
                        .sendToServer(new ConfigValuePacket("FluidLevelEmitter.Value", String.valueOf(reportingValue)));
            }
        } else {
            this.lvlEmitter.setReportingValue(reportingValue);
        }
    }

    @Override
    protected void setupConfig() {
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 0;
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.setRedStoneMode((RedstoneMode) lvlEmitter.getConfigManager().getSetting(Settings.REDSTONE_EMITTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.lvlEmitter.getConfig();
    }
}