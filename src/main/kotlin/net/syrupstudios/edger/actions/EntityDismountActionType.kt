package net.syrupstudios.edger.actions

class EntityDismountActionType : AbstractActionType() {
    override val identifier = "entity-dismount"

    override fun getTranslationType() = "entity"
}
