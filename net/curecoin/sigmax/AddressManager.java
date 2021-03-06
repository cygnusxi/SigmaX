package net.curecoin.sigmax;

/*
 * SigmaX 1.0.0b1 Source Code
 * Copyright (c) 2016 Curecoin Developers
 * Distributed under MIT License
 * Requires Apache Commons Library
 * Supports Java 1.7+
 */

import java.io.*;
import java.util.*;
/**
 * Manages all resources associated with addresses. Holds all address private keys, and keeps track of the offset for the default address.
 * Future plans include tracking the consumed signature offset for all addresses, and expanding this class extensively for full multi-address support.
 */

public class AddressManager
{
    private ArrayList<String> addresses;
    private ArrayList<String> privateKeys;
    private int defaultAddressOffset = 1;

    /**
     * Loads in wallet private key. If none exist, generates an address.
     */
    public AddressManager()
    {
        this.privateKeys = new ArrayList<String>();
        this.addresses = new ArrayList<String>();
        try
        {
            File walletFile = new File("wallet.keys");
            if (!walletFile.exists())
            {
                System.out.println("Generating a new address...");
                String privateKey = getPrivateKey();
                String address = MerkleTreeGenerator.generateMerkleTree(privateKey, 14, 16, 128);
                System.out.println("New address: " + address);
                PrintWriter out = new PrintWriter(walletFile);
                out.println(address + ":" + privateKey);
                out.close();
                addresses.add(address);
                privateKeys.add(privateKey);
            }
            else
            {
                Scanner scan = new Scanner(walletFile);
                while (scan.hasNextLine())
                {
                    String input = scan.nextLine();
                    String address = input.substring(0, input.indexOf(":"));
                    String privateKey = input.substring(input.indexOf(":") + 1);
                    addresses.add(address);
                    privateKeys.add(privateKey);
                }
                scan.close();
            }
            File addressFolder = new File("addresses");
            if (!addressFolder.exists())
            {
                Scanner scan = new Scanner(walletFile);
                while (scan.hasNextLine())
                {
                    String[] combo = scan.nextLine().split(":");
                    MerkleTreeGenerator.generateMerkleTree(combo[1], 14, 16, 128);
                }
                scan.close();
            }
            else
            {
                System.out.println("Don't need to regen address file...");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the address index offset, which is used when signing transactions with the default address. If this were kept track of by the databaseManager, block generation would be tricky.
     * defaultAddressOffset is always at least 1, as it is added to the most-recently-used index (from the blockchain) to calculate which index should be used for signing the next transaction.
     * 
     * @return long The offset for the default address
     */
    public int getDefaultAddressIndexOffset()
    {
        return defaultAddressOffset;
    }

    /**
     * This method increments the defaultAddressIndexOffset in order to account for a signature being used.
     */
    public void incrementDefaultAddressIndexOffset()
    {
        defaultAddressOffset++;
    }

    /**
     * This method resets the defaultAddressOffset to 0; useful when the blockchain has caught up with the transactions we sent. 
     */
    public void resetDefaultAddressIndexOffset()
    {
        defaultAddressOffset = 1;
    }

    /**
     * This method returns the private key of the wallet's default address.
     * 
     * @return String Private key of the daemon's default address.
     */
    public String getDefaultPrivateKey()
    {
        return privateKeys.get(0);
    }

    /**
     * Returns the 'default' address, which is the first one from loading up the address file, or the one that was originally generated with the daemon first ran.
     * 
     * @return String Default address
     */
    public String getDefaultAddress()
    {
        return addresses.get(0);
    }

    /**
     * Returns a new address
     * 
     * @return String A new address
     */
    public String getNewAddress()
    {
        String privateKey = getPrivateKey();
        String address = MerkleTreeGenerator.generateMerkleTree(privateKey, 14, 16, 128);
        addresses.add(address);
        privateKeys.add(privateKey);
        return address;
    }

    /**
     * There is NO WAY this method will appear in any recognizable form in the actual 2.0 release.
     * This IS NOT a safe way to generate private keys--the output of Java's default Random number generator isn't sufficient.
     * Final release will use a variety of system information including mouse movements, timing of process threads, and SecureRandom. 
     * 
     * @return String An insecure private key (seed for Lamport Signatures)
     */
    public String getPrivateKey()
    {
        String characterSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String privateKey = "";
        for (int i = 0; i < 32; i++)
        {
            privateKey += characterSet.charAt(random.nextInt(characterSet.length()));
        }
        return privateKey;
    }
}
