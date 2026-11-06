/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * File IO is kept compatible with Minecraft's NBT API; Arson's JSON config
 * adapter remains responsible for its existing user-facing profile format.
 */
package meteordevelopment.meteorclient.systems;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class System<T> implements ISerializable<T> {
    private final String name;
    private final File file;
    protected boolean isFirstInit;

    protected System(String name) {
        this.name = name;
        this.file = name == null ? null : new File(MeteorClient.FOLDER, name + ".nbt");
        this.isFirstInit = file != null && !file.exists();
    }

    public void init() {
    }

    public void save(File folder) {
        File target = getFile(folder);
        if (target == null) return;
        NbtCompound tag = toTag();
        if (tag == null) return;

        Path temporary = null;
        try {
            File parent = target.getParentFile();
            if (parent != null) Files.createDirectories(parent.toPath());
            temporary = Files.createTempFile(parent == null ? Path.of(".") : parent.toPath(), "arson-", ".tmp");
            NbtIo.write(tag, temporary);
            try {
                Files.move(temporary, target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save Meteor system " + name, exception);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }

    public void save() {
        save(null);
    }

    public void load(File folder) {
        File target = getFile(folder);
        if (target == null || !target.isFile()) return;
        try {
            fromTag(NbtIo.read(target.toPath()));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load Meteor system " + name, exception);
        }
    }

    public void load() {
        load(null);
    }

    private File getFile(File folder) {
        if (file == null) return null;
        return folder == null ? file : new File(folder, file.getName());
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    @Override
    public NbtCompound toTag() {
        return null;
    }

    @Override
    public T fromTag(NbtCompound tag) {
        return null;
    }
}
