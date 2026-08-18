package com.latmod.mods.projectex.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ProjectEXClassTransformer implements IClassTransformer, Opcodes {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        if ("moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory".equals(transformedName)) {
            return transformTransmutationInventory(basicClass);
        } else if ("moze_intel.projecte.gameObjs.container.TransmutationContainer".equals(transformedName)) {
            return transformTransmutationContainer(basicClass);
        } else if ("moze_intel.projecte.gameObjs.container.slots.transmutation.SlotOutput".equals(transformedName)) {
            return transformSlotOutput(basicClass);
        } else if ("moze_intel.projecte.gameObjs.container.slots.transmutation.SlotConsume".equals(transformedName)) {
            return transformSlotConsume(basicClass);
        } else if ("moze_intel.projecte.gameObjs.container.slots.transmutation.SlotLock".equals(transformedName)) {
            return transformSlotLock(basicClass);
        } else if ("moze_intel.projecte.utils.ItemSearchHelper".equals(transformedName)) {
            return transformItemSearchHelper(basicClass);
        } else if ("moze_intel.projecte.gameObjs.gui.GUITransmutation".equals(transformedName)) {
            return transformGUITransmutation(basicClass);
        } else if ("moze_intel.projecte.network.packets.KnowledgeSyncPKT$Handler".equals(transformedName)) {
            return transformKnowledgeSyncPKTHandler(basicClass);
        }

        return basicClass;
    }

    private byte[] transformTransmutationInventory(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (FieldNode field : classNode.fields) {
                field.access = (field.access & ~ACC_PRIVATE) | ACC_PUBLIC;
            }

            for (MethodNode method : classNode.methods) {
                // addEmc(double) -> (D)V
                if (method.name.equals("addEmc") && method.desc.equals("(D)V")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(DLOAD, 1));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleInventoryAddEmc", "(Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;D)V", false));
                    list.add(new InsnNode(RETURN));
                    method.instructions.add(list);
                }

                // hasMaxedEmc() -> ()Z
                if (method.name.equals("hasMaxedEmc") && method.desc.equals("()Z")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleInventoryHasMaxedEmc", "(Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;)Z", false));
                    list.add(new InsnNode(IRETURN));
                    method.instructions.add(list);
                }

                // updateOutputs(boolean) -> updateOutputs(Z)V
                if (method.name.equals("updateOutputs") && method.desc.equals("(Z)V")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ILOAD, 1));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleUpdateOutputs", "(Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;Z)V", false));
                    list.add(new InsnNode(RETURN));
                    method.instructions.add(list);
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformTransmutationContainer(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (FieldNode field : classNode.fields) {
                field.access = (field.access & ~ACC_PRIVATE) | ACC_PUBLIC;
            }

            for (MethodNode method : classNode.methods) {
                // transferStackInSlot(EntityPlayer, int) -> func_82846_b(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;
                if ((method.name.equals("func_82846_b") || method.name.equals("transferStackInSlot")) && method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new VarInsnNode(ILOAD, 2));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleTransferStackInSlot", "(Lmoze_intel/projecte/gameObjs/container/TransmutationContainer;Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;", false));
                    list.add(new InsnNode(ARETURN));
                    method.instructions.add(list);
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformSlotOutput(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (FieldNode field : classNode.fields) {
                field.access = (field.access & ~ACC_PRIVATE) | ACC_PUBLIC;
            }

            for (MethodNode method : classNode.methods) {
                // decrStackSize(int) -> func_75209_a(I)Lnet/minecraft/item/ItemStack;
                if ((method.name.equals("func_75209_a") || method.name.equals("decrStackSize")) && method.desc.equals("(I)Lnet/minecraft/item/ItemStack;")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ILOAD, 1));
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new FieldInsnNode(GETFIELD, "moze_intel/projecte/gameObjs/container/slots/transmutation/SlotOutput", "inv", "Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;"));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleOutputTake", "(Lnet/minecraft/inventory/Slot;ILmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;)Lnet/minecraft/item/ItemStack;", false));
                    list.add(new InsnNode(ARETURN));
                    method.instructions.add(list);
                }

                // canTakeStack(EntityPlayer) -> func_82869_a(Lnet/minecraft/entity/player/EntityPlayer;)Z
                if ((method.name.equals("func_82869_a") || method.name.equals("canTakeStack")) && method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)Z")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new FieldInsnNode(GETFIELD, "moze_intel/projecte/gameObjs/container/slots/transmutation/SlotOutput", "inv", "Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;"));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "canTakeOutput", "(Lnet/minecraft/inventory/Slot;Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;)Z", false));
                    list.add(new InsnNode(IRETURN));
                    method.instructions.add(list);
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformSlotConsume(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (FieldNode field : classNode.fields) {
                field.access = (field.access & ~ACC_PRIVATE) | ACC_PUBLIC;
            }

            for (MethodNode method : classNode.methods) {
                // putStack(ItemStack) -> func_75215_d(Lnet/minecraft/item/ItemStack;)V
                if ((method.name.equals("func_75215_d") || method.name.equals("putStack")) && method.desc.equals("(Lnet/minecraft/item/ItemStack;)V")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new FieldInsnNode(GETFIELD, "moze_intel/projecte/gameObjs/container/slots/transmutation/SlotConsume", "inv", "Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;"));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleConsume", "(Lnet/minecraft/inventory/Slot;Lnet/minecraft/item/ItemStack;Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;)V", false));
                    list.add(new InsnNode(RETURN));
                    method.instructions.add(list);
                }

                // isItemValid(ItemStack) -> func_75214_a(Lnet/minecraft/item/ItemStack;)Z
                if ((method.name.equals("func_75214_a") || method.name.equals("isItemValid")) && method.desc.equals("(Lnet/minecraft/item/ItemStack;)Z")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new FieldInsnNode(GETFIELD, "moze_intel/projecte/gameObjs/container/slots/transmutation/SlotConsume", "inv", "Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;"));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "isConsumeValid", "(Lnet/minecraft/item/ItemStack;Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;)Z", false));
                    list.add(new InsnNode(IRETURN));
                    method.instructions.add(list);
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformSlotLock(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (FieldNode field : classNode.fields) {
                field.access = (field.access & ~ACC_PRIVATE) | ACC_PUBLIC;
            }

            for (MethodNode method : classNode.methods) {
                // putStack(ItemStack) -> func_75215_d(Lnet/minecraft/item/ItemStack;)V
                if ((method.name.equals("func_75215_d") || method.name.equals("putStack")) && method.desc.equals("(Lnet/minecraft/item/ItemStack;)V")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new FieldInsnNode(GETFIELD, "moze_intel/projecte/gameObjs/container/slots/transmutation/SlotLock", "inv", "Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;"));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleLockPutStack", "(Lnet/minecraft/inventory/Slot;Lnet/minecraft/item/ItemStack;Lmoze_intel/projecte/gameObjs/container/inventory/TransmutationInventory;)V", false));
                    list.add(new InsnNode(RETURN));
                    method.instructions.add(list);
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformItemSearchHelper(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                if (method.name.equals("create") && method.desc.equals("(Ljava/lang/String;)Lmoze_intel/projecte/utils/ItemSearchHelper;")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/search/ProjectEXSearchHelper", "create", "(Ljava/lang/String;)Lmoze_intel/projecte/utils/ItemSearchHelper;", false));
                    list.add(new InsnNode(ARETURN));
                    method.instructions.add(list);
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformGUITransmutation(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (FieldNode field : classNode.fields) {
                field.access = (field.access & ~ACC_PRIVATE) | ACC_PUBLIC;
            }

            for (MethodNode method : classNode.methods) {
                // drawGuiContainerForegroundLayer(int, int) -> func_146979_b(II)V
                if ((method.name.equals("func_146979_b") || method.name.equals("drawGuiContainerForegroundLayer")) && method.desc.equals("(II)V")) {
                    method.instructions.clear();
                    method.localVariables = null;
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ILOAD, 1));
                    list.add(new VarInsnNode(ILOAD, 2));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "drawTransmutationForeground", "(Lnet/minecraft/client/gui/inventory/GuiContainer;II)V", false));
                    list.add(new InsnNode(RETURN));
                    method.instructions.add(list);
                }

                // keyTyped(char, int) -> func_73869_a(CI)V
                if ((method.name.equals("func_73869_a") || method.name.equals("keyTyped")) && method.desc.equals("(CI)V")) {
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ILOAD, 1));
                    list.add(new VarInsnNode(ILOAD, 2));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleTransmutationKeyTyped", "(Lnet/minecraft/client/gui/inventory/GuiContainer;CI)Z", false));
                    LabelNode continueLabel = new LabelNode();
                    list.add(new JumpInsnNode(IFEQ, continueLabel));
                    list.add(new InsnNode(RETURN));
                    list.add(continueLabel);
                    method.instructions.insert(list);
                }

                // initGui() -> func_73866_w_()V
                if ((method.name.equals("func_73866_w_") || method.name.equals("initGui")) && method.desc.equals("()V")) {
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleTransmutationInitGui", "(Lnet/minecraft/client/gui/inventory/GuiContainer;)V", false));
                    method.instructions.insert(list);
                }

                // onGuiClosed() -> func_146281_b()V
                if ((method.name.equals("func_146281_b") || method.name.equals("onGuiClosed")) && method.desc.equals("()V")) {
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleTransmutationGuiClosed", "(Lnet/minecraft/client/gui/inventory/GuiContainer;)V", false));
                    method.instructions.insert(list);
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformKnowledgeSyncPKTHandler(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                if (method.name.equals("onMessage")) {
                    for (AbstractInsnNode insn : method.instructions.toArray()) {
                        if (insn.getOpcode() == ARETURN) {
                            InsnList list = new InsnList();
                            list.add(new MethodInsnNode(INVOKESTATIC, "com/latmod/mods/projectex/ProjectEXUtils", "handleKnowledgeSync", "()V", false));
                            method.instructions.insertBefore(insn, list);
                        }
                    }
                }
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }
}
