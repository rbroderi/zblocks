package drzhark.mocreatures.entity;

import com.google.common.base.Optional;
import drzhark.mocreatures.MoCPetData;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.init.MoCItems;
import drzhark.mocreatures.init.MoCSoundEvents;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageHeart;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MoCEntityTameableAquatic extends MoCEntityAquatic implements IMoCTameable {
   protected static final DataParameter OWNER_UNIQUE_ID;
   protected static final DataParameter PET_ID;
   protected static final DataParameter TAMED;
   private boolean hasEaten;
   private int gestationtime;

   public MoCEntityTameableAquatic(World world) {
      super(world);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
      this.dataManager.register(PET_ID, -1);
      this.dataManager.register(TAMED, false);
   }

   public int getOwnerPetId() {
      return (Integer)this.dataManager.get(PET_ID);
   }

   public void setOwnerPetId(int i) {
      this.dataManager.set(PET_ID, i);
   }

   @Nullable
   public UUID getOwnerId() {
      return (UUID)((Optional)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
   }

   public void setOwnerId(@Nullable UUID uniqueId) {
      this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(uniqueId));
   }

   public void setTamed(boolean flag) {
      this.dataManager.set(TAMED, flag);
   }

   public boolean getIsTamed() {
	 /*  int ret=0;
	   try {
	   ret = (int) this.dataManager.get(TAMED);
	   }
	   catch(Exception e){}
	  return ret==0?false:true;
   */
	   //per ryukra comment
	   try {
	   if(this.dataManager.get(TAMED).getClass().equals(boolean.class)) {
		return (boolean)this.dataManager.get(TAMED);    
	   }
	   else if(this.dataManager.get(TAMED).getClass().equals(int.class)) {
		return (int)this.dataManager.get(TAMED)==0;
	   }
	   }
	   catch(Exception e){}
	   return false;
   }

   @Nullable
   public EntityLivingBase getOwner() {
      try {
         UUID uuid = this.getOwnerId();
         return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public boolean attackEntityFrom(DamageSource damagesource, float i) {
      Entity entity = damagesource.getTrueSource();
      if ((!this.isBeingRidden() || entity != this.getRidingEntity()) && (this.getRidingEntity() == null || entity != this.getRidingEntity())) {
         if (this.usesNewAI()) {
            return super.attackEntityFrom(damagesource, i);
         } else {
            return MoCreatures.proxy.enableOwnership && this.getOwnerId() != null && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).getUniqueID().equals(this.getOwnerId()) && !MoCTools.isThisPlayerAnOP((EntityPlayer)entity) ? false : super.attackEntityFrom(damagesource, i);
         }
      } else {
         return false;
      }
   }

   private boolean checkOwnership(EntityPlayer player, EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (this.getIsTamed() && !MoCTools.isThisPlayerAnOP(player)) {
         if (this.getIsGhost() && !stack.isEmpty() && stack.getItem() == MoCItems.petamulet) {
            if (!this.world.isRemote) {
               ((EntityPlayerMP)player).sendAllContents(player.openContainer, player.openContainer.getInventory());
               player.sendMessage(new TextComponentTranslation(TextFormatting.RED + "This pet does not belong to you.", new Object[0]));
            }

            return false;
         } else if (MoCreatures.proxy.enableOwnership && this.getOwnerId() != null && !player.getUniqueID().equals(this.getOwnerId())) {
            player.sendMessage(new TextComponentTranslation(TextFormatting.RED + "This pet does not belong to you.", new Object[0]));
            return false;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public boolean uprocessInteract(EntityPlayer player, EnumHand hand) {
      Boolean tameResult = this.processTameInteract(player, hand);
      return tameResult != null ? tameResult : super.processInteract(player, hand);
   }

   public Boolean processTameInteract(EntityPlayer player, EnumHand hand) {
      if (!this.checkOwnership(player, hand)) {
         return false;
      } else {
         ItemStack stack = player.getHeldItem(hand);
         if (!stack.isEmpty() && this.getIsTamed() && stack.getItem() == MoCItems.scrollOfOwner && MoCreatures.proxy.enableResetOwnership && MoCTools.isThisPlayerAnOP(player)) {
            stack.shrink(1);
            if (stack.isEmpty()) {
               player.setHeldItem(hand, ItemStack.EMPTY);
            }

            if (!this.world.isRemote) {
               if (this.getOwnerPetId() != -1) {
                  MoCreatures.instance.mapData.removeOwnerPet(this, this.getOwnerPetId());
               }

               this.setOwnerId((UUID)null);
            }

            return true;
         } else if (MoCreatures.proxy.enableOwnership && this.getOwnerId() != null && !player.getUniqueID().equals(this.getOwnerId()) && !MoCTools.isThisPlayerAnOP(player)) {
            return true;
         } else if (!this.world.isRemote && !stack.isEmpty() && this.getIsTamed() && (stack.getItem() == MoCItems.medallion || stack.getItem() == Items.BOOK || stack.getItem() == Items.NAME_TAG)) {
            return MoCTools.tameWithName(player, this) ? true : false;
         } else if (!stack.isEmpty() && this.getIsTamed() && stack.getItem() == MoCItems.scrollFreedom) {
            stack.shrink(1);
            if (stack.isEmpty()) {
               player.setHeldItem(hand, ItemStack.EMPTY);
            }

            if (!this.world.isRemote) {
               if (this.getOwnerPetId() != -1) {
                  MoCreatures.instance.mapData.removeOwnerPet(this, this.getOwnerPetId());
               }

               this.setOwnerId((UUID)null);
               this.setPetName("");
               this.dropMyStuff();
               this.setTamed(false);
            }

            return true;
         } else if (!stack.isEmpty() && this.getIsTamed() && stack.getItem() == MoCItems.scrollOfSale) {
            stack.shrink(1);
            if (stack.isEmpty()) {
               player.setHeldItem(hand, ItemStack.EMPTY);
            }

            if (!this.world.isRemote) {
               if (this.getOwnerPetId() != -1) {
                  MoCreatures.instance.mapData.removeOwnerPet(this, this.getOwnerPetId());
               }

               this.setOwnerId((UUID)null);
            }

            return true;
         } else if (!stack.isEmpty() && this.getIsTamed() && this.isMyHealFood(stack)) {
            stack.shrink(1);
            if (stack.isEmpty()) {
               player.setHeldItem(hand, ItemStack.EMPTY);
            }

            MoCTools.playCustomSound(this, MoCSoundEvents.ENTITY_GENERIC_EATING);
            if (!this.world.isRemote) {
               this.setHealth(this.getMaxHealth());
            }

            return true;
         } else if (!stack.isEmpty() && stack.getItem() == MoCItems.fishnet && stack.getItemDamage() == 0 && this.canBeTrappedInNet()) {
            if (!this.world.isRemote) {
               MoCPetData petData = MoCreatures.instance.mapData.getPetData(this.getOwnerId());
               if (petData != null) {
                  petData.setInAmulet(this.getOwnerPetId(), true);
               }
            }

            player.setHeldItem(hand, ItemStack.EMPTY);
            if (!this.world.isRemote) {
               MoCTools.dropAmulet(this, 1, player);
               this.isDead = true;
            }

            return true;
         } else {
            return null;
         }
      }
   }

   public void setDead() {
      if (this.world.isRemote || !this.getIsTamed() || this.getHealth() <= 0.0F || this.riderIsDisconnecting) {
         super.setDead();
      }
   }

   public void playTameEffect(boolean par1) {
      EnumParticleTypes particleType = EnumParticleTypes.HEART;
      if (!par1) {
         particleType = EnumParticleTypes.SMOKE_NORMAL;
      }

      for(int i = 0; i < 7; ++i) {
         double d0 = this.rand.nextGaussian() * 0.02D;
         double d1 = this.rand.nextGaussian() * 0.02D;
         double d2 = this.rand.nextGaussian() * 0.02D;
         this.world.spawnParticle(particleType, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2, new int[0]);
      }

   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setBoolean("Tamed", this.getIsTamed());
      if (this.getOwnerId() != null) {
         nbttagcompound.setString("OwnerUUID", this.getOwnerId().toString());
      }

      if (this.getOwnerPetId() != -1) {
         nbttagcompound.setInteger("PetId", this.getOwnerPetId());
      }

      if (this instanceof IMoCTameable && this.getIsTamed() && MoCreatures.instance.mapData != null) {
         MoCreatures.instance.mapData.updateOwnerPet(this);
      }

   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
	  super.readEntityFromNBT(nbttagcompound);
      this.setTamed(nbttagcompound.getBoolean("Tamed"));
      String s = "";
      if (nbttagcompound.hasKey("OwnerUUID", 8)) {
         s = nbttagcompound.getString("OwnerUUID");
      }

      if (!s.isEmpty()) {
         this.setOwnerId(UUID.fromString(s));
      }

      if (nbttagcompound.hasKey("PetId")) {
         this.setOwnerPetId(nbttagcompound.getInteger("PetId"));
      }

      if (this.getIsTamed() && nbttagcompound.hasKey("PetId")) {
         MoCPetData petData = MoCreatures.instance.mapData.getPetData(this.getOwnerId());
         if (petData != null) {
            NBTTagList tag = petData.getOwnerRootNBT().getTagList("TamedList", 10);

            for(int i = 0; i < tag.tagCount(); ++i) {
               NBTTagCompound nbt = tag.getCompoundTagAt(i);
               if (nbttagcompound.getInteger("PetId") == nbttagcompound.getInteger("PetId")) {
                  nbt.setBoolean("InAmulet", false);
                  if (nbt.hasKey("Cloned")) {
                     nbt.removeTag("Cloned");
                     this.setTamed(false);
                     this.setDead();
                  }
               }
            }
         } else {
            this.setOwnerPetId(-1);
         }
      }

   }

   public boolean shouldDismountInWater(Entity rider) {
      return !this.getIsTamed();
   }

   public boolean isBreedingItem(ItemStack par1ItemStack) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte par1) {
      if (par1 == 2) {
        limbSwingAmount = 1.5F;
         this.hurtResistantTime = this.maxHurtResistantTime;
        hurtTime = this.maxHurtTime = 10;
         this.attackedAtYaw = 0.0F;
         this.playSound(this.getHurtSound(DamageSource.GENERIC), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         this.attackEntityFrom(DamageSource.GENERIC, 0.0F);
      } else if (par1 == 3) {
         this.playSound(this.getDeathSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         this.setHealth(0.0F);
         this.onDeath(DamageSource.GENERIC);
      }

   }

   public float getPetHealth() {
      return this.getHealth();
   }

   public boolean isRiderDisconnecting() {
      return this.riderIsDisconnecting;
   }

   public void setRiderDisconnecting(boolean flag) {
      this.riderIsDisconnecting = flag;
   }

   public void spawnHeart() {
      double var2 = this.rand.nextGaussian() * 0.02D;
      double var4 = this.rand.nextGaussian() * 0.02D;
      double var6 = this.rand.nextGaussian() * 0.02D;
      this.world.spawnParticle(EnumParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, var2, var4, var6, new int[0]);
   }

   public boolean readytoBreed() {
      return !this.isBeingRidden() && this.getRidingEntity() == null && this.getIsTamed() && this.getHasEaten() && this.getIsAdult();
   }

   public String getOffspringClazz(IMoCTameable mate) {
      return "";
   }

   public int getOffspringTypeInt(IMoCTameable mate) {
      return 0;
   }

   public boolean compatibleMate(Entity mate) {
      return mate instanceof IMoCTameable;
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (!this.world.isRemote && this.readytoBreed() && this.rand.nextInt(100) == 0) {
         this.doBreeding();
      }

   }

   public boolean isEntityInsideOpaqueBlock() {
      return this.getIsTamed() ? isEntityInsideOpaqueBlock():false;
   }

   protected void doBreeding() {
      int i = 0;
      List list = this.world.getEntitiesWithinAABBExcludingEntity(this,getEntityBoundingBox().expand(8.0D, 3.0D, 8.0D));

      for(int j = 0; j < list.size(); ++j) {
         Entity entity = (Entity)list.get(j);
         if (this.compatibleMate(entity)) {
            ++i;
         }
      }

      if (i <= 1) {
         List list1 = this.world.getEntitiesWithinAABBExcludingEntity(this,getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D));
         int k = 0;

         while(true) {
            label62: {
               if (k < list1.size()) {
                  Entity mate = (Entity)list1.get(k);
                  if (!this.compatibleMate(mate) || mate == this) {
                     break label62;
                  }

                  if (!this.readytoBreed()) {
                     return;
                  }

                  if (!((IMoCTameable)mate).readytoBreed()) {
                     return;
                  }

                  this.setGestationTime(this.getGestationTime() + 1);
                  if (!this.world.isRemote) {
                     MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageHeart(this.getEntityId()), new TargetPoint(this.world.provider.getDimensionType().getId(), this.posX, this.posY, this.posZ, 64.0D));
                  }

                  if (this.getGestationTime() <= 50) {
                     break label62;
                  }

                  try {
                     String offspringName = this.getOffspringClazz((IMoCTameable)mate);
                     EntityLiving offspring = (EntityLiving)EntityList.createEntityByIDFromName(new ResourceLocation("mocreatures:" + offspringName.toLowerCase()),world); //world
                     if (offspring != null && offspring instanceof IMoCTameable) {
                        IMoCTameable baby = (IMoCTameable)offspring;
                        ((EntityLiving)baby).setPosition(this.posX, this.posY, this.posZ);
                        this.world.spawnEntity((EntityLiving)baby);
                        baby.setAdult(false);
                        baby.setEdad(35);
                        baby.setTamed(true);
                        baby.setOwnerId(this.getOwnerId());
                        baby.setType(this.getOffspringTypeInt((IMoCTameable)mate));
                        EntityPlayer entityplayer = this.world.getPlayerEntityByUUID(this.getOwnerId());
                        if (entityplayer != null) {
                           MoCTools.tameWithName(entityplayer, baby);
                        }
                     }

                   //  MoCTools.playCustomSound(this, SounENTITY_CHICKEN_EGG); no idea how to fix this Sound... perhaps
                  } catch (Exception var10) {
                  }

                  this.setHasEaten(false);
                  this.setGestationTime(0);
                  ((IMoCTameable)mate).setHasEaten(false);
                  ((IMoCTameable)mate).setGestationTime(0);
               }

               return;
            }

            ++k;
         }
      }
   }

   public void setHasEaten(boolean flag) {
      this.hasEaten = flag;
   }

   public boolean getHasEaten() {
      return this.hasEaten;
   }

   public void setGestationTime(int time) {
      this.gestationtime = time;
   }

   public int getGestationTime() {
      return this.gestationtime;
   }

   static {
      OWNER_UNIQUE_ID = EntityDataManager.createKey(MoCEntityTameableAquatic.class, DataSerializers.OPTIONAL_UNIQUE_ID);
      PET_ID = EntityDataManager.createKey(MoCEntityTameableAquatic.class, DataSerializers.VARINT);
      TAMED = EntityDataManager.createKey(MoCEntityTameableAquatic.class, DataSerializers.BOOLEAN);
   }
}
