package site.remlit.aster.event.policy

import site.remlit.aster.common.model.Policy

/**
 * Event fired when a policy is created
 *
 * @since 2025.12.2.2-SNAPSHOT
 * */
class PolicyCreateEvent(policy: Policy) : PolicyEvent(policy)
