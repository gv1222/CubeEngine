package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author Anselm Brehme
 */
public class EnchantMatcher
{
    private THashMap<String, Enchantment> enchantments;
    private static EnchantMatcher instance = null;

    private EnchantMatcher()
    {
        this.enchantments = new THashMap<String, Enchantment>();

        TIntObjectHashMap<List<String>> enchs = this.readEnchantments();
        for (int id : enchs.keys())
        {
            this.registerEnchantment(Enchantment.getById(id), enchs.get(id));
        }
    }

    public static EnchantMatcher get()
    {
        if (instance == null)
        {
            instance = new EnchantMatcher();
        }
        return instance;
    }

    public final void registerEnchantment(Enchantment ench, List<String> names)
    {
        for (String s : names)
        {
            this.enchantments.put(s.toLowerCase(Locale.ENGLISH), ench);
        }
    }

    public Enchantment matchEnchantment(String s)
    {
        Enchantment ench = this.enchantments.get(s.toLowerCase(Locale.ENGLISH));
        try
        {
            int enchId = Integer.parseInt(s);
            return Enchantment.getById(enchId);
        }
        catch (NumberFormatException e)
        {}
        if (ench == null)
        {
            if (s.length() < 4)
            {
                return null;
            }
            String t_key = null;
            for (String key : this.enchantments.keySet())
            {
                int ld = StringUtils.getLevenshteinDistance(s.toLowerCase(Locale.ENGLISH), key);
                if (ld == 1)
                {
                    return this.enchantments.get(key);
                }
                if (ld <= 2)
                {
                    t_key = key;
                }
            }
            if (t_key != null)
            {
                return this.enchantments.get(t_key);
            }
        }
        return ench;
    }

    private TIntObjectHashMap<List<String>> readEnchantments()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENCHANTMENTS.getTarget())));
            TIntObjectHashMap<List<String>> enchs = new TIntObjectHashMap<List<String>>();
            String line;
            ArrayList<String> names = new ArrayList<String>();
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }
                if (line.endsWith(":"))
                {
                    int id = Integer.parseInt(line.substring(0, line.length() - 1));
                    names = new ArrayList<String>();
                    enchs.put(id, names);
                }
                else
                {
                    names.add(line);
                }
            }
            return enchs;
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while reading enchantments.txt", ex);
        }
    }
}