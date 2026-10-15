package net.syrupstudios.edger.actions

class EntityMountActionType : AbstractActionType() {
    override val identifier = "entity-mount"

    override fun getTranslationType() = "entity"
}
