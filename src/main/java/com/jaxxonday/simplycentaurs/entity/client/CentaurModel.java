package com.jaxxonday.simplycentaurs.entity.client;

import com.jaxxonday.simplycentaurs.entity.animations.CentaurAnimationDefinitions;
import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CentaurModel<T extends Entity> extends HierarchicalModel<T> {
	private int savedGender = 0;
	public ModelPart centaur;
	public ModelPart body;
	public ModelPart frontBody;
	public ModelPart head;
	public ModelPart waist;
	public ModelPart chest;
	private final ModelPart chestMale;
	private final ModelPart chestFemale;
	private final ModelPart waistMale;
	private final ModelPart waistFemale;
	public ModelPart rArm;
	public ModelPart lArm;
	private final ModelPart rArmMale;
	private final ModelPart rArmFemale;

	private final ModelPart lArmMale;
	private final ModelPart lArmFemale;

	public ModelPart rHandWeapon;
	//public final ModelPart lHandWeapon;

	public CentaurModel(ModelPart root) {
		this.centaur = root.getChild("centaur");
		this.body = this.centaur.getChild("body");
		this.frontBody = this.body.getChild("front_body");
		this.waist = this.frontBody.getChild("waist");
		this.waistMale = this.waist.getChild("waist_male");
		this.waistFemale = this.waist.getChild("waist_fem");
		this.chest = this.waist.getChild("chest");
		this.head = this.chest.getChild("head");
		this.chestMale = this.chest.getChild("chest_male");
		this.chestFemale = this.chest.getChild("chest_fem");
		this.rArm = this.chest.getChild("r_arm");
		this.lArm = this.chest.getChild("l_arm");

		this.rArmMale = this.rArm.getChild("r_arm_male");
		this.rArmFemale = this.rArm.getChild("r_arm_fem");

		this.lArmMale = this.lArm.getChild("l_arm_male");
		this.lArmFemale = this.lArm.getChild("l_arm_fem");

		this.rHandWeapon= this.rArm.getChild("r_hand_weapon");
		//this.lHandWeapon = this.lArm.getChild("l_hand_weapon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition centaur = partdefinition.addOrReplaceChild("centaur", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition body = centaur.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition front_body = body.addOrReplaceChild("front_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition front_torso = front_body.addOrReplaceChild("front_torso", CubeListBuilder.create().texOffs(20, 35).addBox(-3.0F, 0.0F, -1.5F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, -3.0F, -1.5F));

		PartDefinition armor_hip = front_torso.addOrReplaceChild("armor_hip", CubeListBuilder.create().texOffs(106, 62).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition frontlegs = front_torso.addOrReplaceChild("frontlegs", CubeListBuilder.create(), PartPose.offset(1.5F, 5.0F, 0.0F));

		PartDefinition r_leg = frontlegs.addOrReplaceChild("r_leg", CubeListBuilder.create().texOffs(0, 60).addBox(-1.4F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(-0.001F))
				.texOffs(-1, 77).addBox(-1.5F, 10.7F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.31F)), PartPose.offset(-3.0F, 1.0F, 0.0F));

		PartDefinition armor_r_leg = r_leg.addOrReplaceChild("armor_r_leg", CubeListBuilder.create().texOffs(78, 93).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offset(0.1F, 0.0F, 0.0F));

		PartDefinition l_leg = frontlegs.addOrReplaceChild("l_leg", CubeListBuilder.create().texOffs(13, 60).addBox(-1.4F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(-0.001F))
				.texOffs(13, 77).addBox(-1.5F, 10.7F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.31F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition armor_l_leg = l_leg.addOrReplaceChild("armor_l_leg", CubeListBuilder.create().texOffs(91, 93).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offset(0.1F, 0.0F, 0.0F));

		PartDefinition waist = front_body.addOrReplaceChild("waist", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.5F));

		PartDefinition waist_fem = waist.addOrReplaceChild("waist_fem", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 5.0F, 3.0F, new CubeDeformation(-0.05F)), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition armor_chestplate_bottom_fem = waist_fem.addOrReplaceChild("armor_chestplate_bottom_fem", CubeListBuilder.create().texOffs(99, 17).addBox(-3.0F, -2.0F, -1.5F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition waist_male = waist.addOrReplaceChild("waist_male", CubeListBuilder.create().texOffs(41, 46).addBox(-3.0F, -5.0F, -2.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, -3.0F, 0.5F));

		PartDefinition armor_chestplate_bottom_male = waist_male.addOrReplaceChild("armor_chestplate_bottom_male", CubeListBuilder.create().texOffs(98, 35).addBox(-3.0F, -2.0F, -1.5F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, -2.0F, -0.5F));

		PartDefinition chest = waist.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 1.5F));

		PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(33, 8).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, -3.0F, -1.0F));

		PartDefinition chest_fem = chest.addOrReplaceChild("chest_fem", CubeListBuilder.create().texOffs(25, 24).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest_b_fem_r1 = chest_fem.addOrReplaceChild("chest_b_fem_r1", CubeListBuilder.create().texOffs(44, 25).addBox(-3.0F, -1.5F, -1.5F, 6.0F, 3.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, -0.9544F, -2.8995F, 1.0472F, 0.0F, 0.0F));

		PartDefinition armor_chestplate_top_fem = chest_fem.addOrReplaceChild("armor_chestplate_top_fem", CubeListBuilder.create().texOffs(99, 0).addBox(-3.0F, -7.0F, -1.5F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 3.0F, -1.5F));

		PartDefinition armor_chest_b_fem = armor_chestplate_top_fem.addOrReplaceChild("armor_chest_b_fem", CubeListBuilder.create(), PartPose.offset(0.0F, -3.9544F, -1.3995F));

		PartDefinition armor_chest_b_fem_r1 = armor_chest_b_fem.addOrReplaceChild("armor_chest_b_fem_r1", CubeListBuilder.create().texOffs(99, 9).addBox(-3.0F, -1.5F, -1.5F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));

		PartDefinition chest_male = chest.addOrReplaceChild("chest_male", CubeListBuilder.create().texOffs(62, 47).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition armor_chestplate_top_male = chest_male.addOrReplaceChild("armor_chestplate_top_male", CubeListBuilder.create().texOffs(98, 25).addBox(-3.0F, -7.0F, -1.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 3.0F, -1.5F));

		PartDefinition r_arm = chest.addOrReplaceChild("r_arm", CubeListBuilder.create(), PartPose.offset(-4.0F, -3.5F, -1.0F));

		PartDefinition r_arm_fem = r_arm.addOrReplaceChild("r_arm_fem", CubeListBuilder.create().texOffs(0, 43).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition armor_r_shoulder_pad_fem = r_arm_fem.addOrReplaceChild("armor_r_shoulder_pad_fem", CubeListBuilder.create().texOffs(65, 27).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.21F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition r_arm_male = r_arm.addOrReplaceChild("r_arm_male", CubeListBuilder.create().texOffs(0, 43).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.2F)), PartPose.offset(-0.2F, 0.2F, 0.1F));

		PartDefinition armor_r_shoulder_pad_male = r_arm_male.addOrReplaceChild("armor_r_shoulder_pad_male", CubeListBuilder.create().texOffs(65, 27).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.41F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition r_hand_weapon = r_arm.addOrReplaceChild("r_hand_weapon", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.5F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition l_arm = chest.addOrReplaceChild("l_arm", CubeListBuilder.create(), PartPose.offset(4.0F, -3.5F, -1.0F));

		PartDefinition l_arm_fem = l_arm.addOrReplaceChild("l_arm_fem", CubeListBuilder.create().texOffs(9, 43).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition armor_l_shoulder_pad_fem = l_arm_fem.addOrReplaceChild("armor_l_shoulder_pad_fem", CubeListBuilder.create().texOffs(74, 27).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.21F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition l_arm_male = l_arm.addOrReplaceChild("l_arm_male", CubeListBuilder.create().texOffs(9, 43).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.2F)), PartPose.offset(0.2F, 0.2F, 0.1F));

		PartDefinition armor_l_shoulder_pad_male = l_arm_male.addOrReplaceChild("armor_l_shoulder_pad_male", CubeListBuilder.create().texOffs(74, 27).addBox(-0.6F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.41F)), PartPose.offset(-0.4F, 1.0F, 0.0F));

		PartDefinition l_hand_weapon = l_arm.addOrReplaceChild("l_hand_weapon", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, 0.0F));

		PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -1.0F));

		PartDefinition armor_helmet = head.addOrReplaceChild("armor_helmet", CubeListBuilder.create().texOffs(65, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.2F))
				.texOffs(65, 18).addBox(-4.0F, -10.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(73, 18).addBox(2.0F, -10.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(24, 0).addBox(-3.0F, -10.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition ears = head.addOrReplaceChild("ears", CubeListBuilder.create().texOffs(52, 2).addBox(-4.0F, -2.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
				.texOffs(52, 8).addBox(2.0F, -2.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, -7.5F, 0.0F));

		PartDefinition rear_body = body.addOrReplaceChild("rear_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition waist2 = rear_body.addOrReplaceChild("waist2", CubeListBuilder.create().texOffs(41, 35).addBox(-2.0F, -2.5F, -2.5F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, 0.5F, 3.5F));

		PartDefinition saddle = waist2.addOrReplaceChild("saddle", CubeListBuilder.create().texOffs(63, 33).addBox(-3.0F, -3.0F, -3.5F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.55F)), PartPose.offset(0.0F, -0.5F, 1.0F));

		PartDefinition armor_waist2 = waist2.addOrReplaceChild("armor_waist2", CubeListBuilder.create().texOffs(82, 60).addBox(-3.0F, -3.0F, -3.5F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.52F)), PartPose.offset(0.0F, -0.5F, 1.0F));

		PartDefinition rear_torso = rear_body.addOrReplaceChild("rear_torso", CubeListBuilder.create().texOffs(20, 46).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 9.0F));

		PartDefinition armor_hip2 = rear_torso.addOrReplaceChild("armor_hip2", CubeListBuilder.create().texOffs(106, 77).addBox(-3.0F, -4.0F, -1.5F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, -0.5F));

		PartDefinition backlegs = rear_torso.addOrReplaceChild("backlegs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition l_leg2 = backlegs.addOrReplaceChild("l_leg2", CubeListBuilder.create().texOffs(39, 60).addBox(-1.4F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(-0.001F))
				.texOffs(39, 77).addBox(-1.4F, 10.7F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.31F)), PartPose.offset(1.5F, 3.0F, 0.0F));

		PartDefinition armor_l_leg2 = l_leg2.addOrReplaceChild("armor_l_leg2", CubeListBuilder.create().texOffs(91, 78).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offset(0.1F, 0.0F, 0.0F));

		PartDefinition r_leg2 = backlegs.addOrReplaceChild("r_leg2", CubeListBuilder.create().texOffs(26, 60).addBox(-1.4F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(-0.001F))
				.texOffs(26, 77).addBox(-1.5F, 10.7F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.31F)), PartPose.offset(-1.5F, 3.0F, 0.0F));

		PartDefinition armor_r_leg2 = r_leg2.addOrReplaceChild("armor_r_leg2", CubeListBuilder.create().texOffs(78, 78).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offset(0.1F, 0.0F, 0.0F));

		PartDefinition tail = backlegs.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 8).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 3.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		CentaurEntity centaurEntity = ((CentaurEntity) entity);
		if(centaurEntity.isBeingRidden()) {
			this.animateWalk(CentaurAnimationDefinitions.WALK_BASE, limbSwing, limbSwingAmount, 2.0f, 2.5f);
			this.animate(centaurEntity.idleAnimationState, CentaurAnimationDefinitions.IDLE_ARM_EXTENDED, ageInTicks, 0.5f);
		} else {
			if(centaurEntity.getIsAiming()) {
				this.animateWalk(CentaurAnimationDefinitions.WALK_BASE, limbSwing, limbSwingAmount, 1.5f, 2.5f);
				this.animate(centaurEntity.idleAnimationState, CentaurAnimationDefinitions.ANY_ARM_BOW, ageInTicks, 0.5f);
			} else {
				this.animateWalk(CentaurAnimationDefinitions.WALK, limbSwing, limbSwingAmount, 1.5f, 2.5f);
				this.animate(centaurEntity.idleAnimationState, CentaurAnimationDefinitions.IDLE, ageInTicks, 0.5f);
				//this.animate(centaurEntity.idleAnimationState, CentaurAnimationDefinitions.ANY_ARM_WAIST_ATTACK, ageInTicks, 2f);
			}

		}

		this.animate(centaurEntity.attackAnimationState, CentaurAnimationDefinitions.ANY_ARM_WAIST_ATTACK, ageInTicks, 3f);
		this.animate(centaurEntity.jumpAnimationState, CentaurAnimationDefinitions.RAISE_BASE, ageInTicks, 2.7f);
		//centaurEntity.heightBoost = (-(this.body.y) + 4.0d) * 0.0625d;

		//this.animate(centaurEntity.idleAnimationState, CentaurAnimationDefinitions.ANY_ARM_WAIST_ATTACK, ageInTicks, 2f);
	}

	private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
		pNetHeadYaw = Mth.clamp(pNetHeadYaw, -70.0f, 70.0f);
		pHeadPitch = Mth.clamp(pHeadPitch, -35.0f, 55.0f);
		this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180f);
		this.head.xRot = pHeadPitch * ((float) Math.PI / 180f);
	}

	public void setGenderVisible(int pGender) {
		// Check to see if we need to change visibility
		if(this.savedGender == pGender) {
			return;
		}
		this.savedGender = pGender;

		if(pGender == 1) {
			this.chestMale.visible = false;
			this.waistMale.visible = false;
			this.rArmMale.visible = false;
			this.lArmMale.visible = false;

			this.chestFemale.visible = true;
			this.waistFemale.visible = true;
			this.rArmFemale.visible = true;
			this.lArmFemale.visible = true;
		} else if (pGender == 2){
			this.chestFemale.visible = false;
			this.waistFemale.visible = false;
			this.rArmFemale.visible = false;
			this.lArmFemale.visible = false;

			this.chestMale.visible = true;
			this.waistMale.visible = true;
			this.rArmMale.visible = true;
			this.lArmMale.visible = true;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.centaur.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public PoseStack applyPoseStackTransformations(PoseStack poseStack, boolean applyRHandWeapon) {
		this.centaur.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		this.frontBody.translateAndRotate(poseStack);
		this.waist.translateAndRotate(poseStack);
		this.chest.translateAndRotate(poseStack);
		this.rArm.translateAndRotate(poseStack);
		if(applyRHandWeapon) {
			this.rHandWeapon.translateAndRotate(poseStack);
		}
		return poseStack;
	}


	public PoseStack applyRHandWeaponTransformation(PoseStack poseStack) {
		this.rHandWeapon.translateAndRotate(poseStack);
		return poseStack;
	}

	@Override
	public ModelPart root() {
		return centaur;
	}
}