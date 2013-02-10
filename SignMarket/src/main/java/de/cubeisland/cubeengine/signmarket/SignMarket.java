package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockManager;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoManager;


public class Signmarket extends Module
{
    private Conomy conomy;
    private SignMarketBlockManager smblockManager;
    private SignMarketInfoManager sminfoManager;
    private MarketSignInventoryListener inventoryListener;


    @Override
    public void onEnable()
    {
        this.smblockManager = new SignMarketBlockManager(this);
        this.sminfoManager = new SignMarketInfoManager(this);

        this.registerListener(new MarketSignListener());
        this.inventoryListener = new MarketSignInventoryListener(this);
        this.registerListener(this.inventoryListener);
    }

    public MarketSignInventoryListener getInventoryListener() {
        return inventoryListener;
    }

    public Conomy getConomy() {
        return conomy;
    }

    public SignMarketBlockManager getSmblockManager() {
        return smblockManager;
    }

    public SignMarketInfoManager getSminfoManager() {
        return sminfoManager;
    }
}
