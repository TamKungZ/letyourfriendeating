# Compatibility Status and Future Support

## Will there be support for Minecraft **1.20.5 - 1.20.6 (Forge/Fabric)?**

**No, and here’s why:**  
Since Minecraft 1.20.5, several critical components related to `Entity` and `FoodComponent` have either been **removed**, **moved**, or **reworked** significantly. This makes updating very difficult, especially when it comes to understanding the new internal behavior.

As a result, I’ve decided that version **1.0.3 will not support Minecraft 1.20.5+** on Forge/Fabric.  
The fact that there’s even a **NeoForge version for 1.20.6 (v1.0.1)** is already quite lucky — please treat that as a **bonus**!

If I were to make a compatible version, it would probably be an ultra-basic **“1.0.0 Alpha”**, where right-clicking feeds other players with **no effects, no animations, and no compatibility** with other mods.  
In short: **not worth it for now**.

---

## What about the `Spigot` branch on GitHub?

Yes — I’m **experimenting** with a plugin version for Spigot servers. It’s still a work in progress because I’m learning how server-side behavior works.

Right now, it’s already **functional**, even though Spigot lacks `FoodComponent`.  
I manually set up food behaviors and included a **config file** so users can define custom food items.

Luckily, vanilla Minecraft doesn't have too many food items, so things are manageable.

---

## Will there be a **1.21.x** version?

That’s the goal — and technically, it’s **not impossible**.  
Right now, I’ve only managed to get it working on **Fabric (Yarn mappings)** because updated Forge documentation is not available.

So far:

- **Fabric 1.21.4** → `v1.0.1 Alpha`  
- **Fabric 1.21.6** → `v1.0.2 Beta`

They work — but required lots of effort to maintain compatibility.

---

## Will you support **versions below 1.16.5**?

**Probably not**, and here’s why:

It’s not beyond my capabilities, but it’s honestly **not worth the effort**.  
Older versions like **1.12.2** are still somewhat popular, so **maybe**, but anything before that (e.g., 1.7.10) is off the table.

The main challenges are:

- **Code structure and package names** in older versions are extremely outdated  
- The conversion process back to older APIs is **complex and messy**  
- Debugging and testing for outdated modloaders is time-consuming and not enjoyable

So unless there's a **very high demand**, don't expect official support for anything pre-1.12.2.

---
 Thanks for all the feedback and interest!  
This project is powered by curiosity and community support — and your input helps shape where it goes next!
