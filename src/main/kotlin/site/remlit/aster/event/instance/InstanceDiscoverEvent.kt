package site.remlit.aster.event.instance

import site.remlit.aster.common.model.Instance

/**
 * Event fired when an instance is discovered
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class InstanceDiscoverEvent(instance: Instance) : InstanceEvent(instance)
