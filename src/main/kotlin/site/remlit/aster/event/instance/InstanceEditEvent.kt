package site.remlit.aster.event.instance

import site.remlit.aster.common.model.Instance


/**
 * Event fired when an instance is edited
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class InstanceEditEvent(instance: Instance) : InstanceEvent(instance)
