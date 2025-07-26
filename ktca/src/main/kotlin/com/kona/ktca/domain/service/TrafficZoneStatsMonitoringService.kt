package com.kona.ktca.domain.service

import com.kona.common.infrastructure.util.*
import com.kona.ktca.domain.dto.TrafficZoneStatsMonitorDTO
import com.kona.ktca.domain.event.TrafficZoneMonitorSavedEvent
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.domain.port.inbound.TrafficZoneStatsMonitorPort
import com.kona.ktca.domain.port.outbound.TrafficZoneStatsMonitorRepository
import com.kona.ktca.dto.StatsType
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class TrafficZoneStatsMonitoringService(
    private val trafficZoneStatsMonitorRepository: TrafficZoneStatsMonitorRepository
): TrafficZoneStatsMonitorPort {

    /**
     * statsType (분, 시간, 일, 월) 별에 따른 통계 조회
     * totalEntryCount 의 경우 통계 데이터 적재 시 일 별로 초기화 되는 값 그대로 저장되어, 이를 조회 시 적절하게 가공하여 응답해야 한다.
     *      분, 시간 -> 이전 시간대의 값과 비교해 뺀 값
     *      일 -> 그대로 응답
     *      월 -> 해당 월 내 일 데이터 전체 값 합산
     */
    override suspend fun statsMonitor(statsType: StatsType, zoneId: String, startDate: String, endDate: String): List<TrafficZoneStatsMonitor> {
        val find: List<TrafficZoneStatsMonitor> =  trafficZoneStatsMonitorRepository.findAllByPredicate(
            dto = TrafficZoneStatsMonitorDTO(
                statsType = if(statsType == StatsType.MONTH) StatsType.DAY else statsType,
                zoneId = zoneId,
                startDate = when(statsType) {
                    StatsType.MINUTE, StatsType.HOUR -> oneUnitBeforeStatsDate(startDate, statsType)
                    StatsType.DAY -> startDate
                    StatsType.MONTH -> convertFirstDayOfMonth(yyyyMM = startDate)
                },
                endDate = when(statsType) {
                    StatsType.MONTH -> convertLastDayOfMonth(yyyyMM = endDate)
                    else -> endDate
                },
            )
        )

        return when(statsType) {
            StatsType.MINUTE, StatsType.HOUR -> {
                val resultList = mutableListOf<TrafficZoneStatsMonitor>()
                for (i in 1 until find.size) {
                    val current = find[i]
                    val previous = find[i - 1]
                    val calculatedEntryCount: Long = if(current.statsDate!!.substring(0, 8) == previous.statsDate!!.substring(0, 8)) {
                        current.totalEntryCount - previous.totalEntryCount
                    } else {
                        current.totalEntryCount
                    }
                    resultList.add(current.copy(totalEntryCount = calculatedEntryCount))
                }
                resultList
            }
            StatsType.DAY -> find
            StatsType.MONTH -> {
                find.groupBy { it.statsDate!!.substring(0, 6) }
                    .map { (month, dailyStatsList) ->
                        val firstItem = dailyStatsList.first()
                        TrafficZoneStatsMonitor(
                            zoneId = firstItem.zoneId,
                            zoneAlias = firstItem.zoneAlias,
                            statsType = StatsType.MONTH,
                            statsDate = month,
                            totalEntryCount = dailyStatsList.sumOf { it.totalEntryCount },
                            maxThreshold = dailyStatsList.maxOf { it.maxThreshold },
                            maxWaitingCount = dailyStatsList.maxOf { it.maxWaitingCount },
                            maxEstimatedClearTime = dailyStatsList.maxOf { it.maxEstimatedClearTime },
                        )
                    }
            }
        }
    }

    override suspend fun applySaveEvent(now: LocalDateTime, event: TrafficZoneMonitorSavedEvent) {
        val statsDateStrings = statsDateStrings(now)
        val bulkSave = mutableListOf<TrafficZoneStatsMonitor>()

        event.trafficZoneMonitors.forEach { zoneEvent ->
            var currentDomains = findOrCreateMissingStatsMonitors(zoneEvent.zoneId, statsDateStrings, zoneEvent.zoneAlias)

            currentDomains = updateDomainMetrics(currentDomains, zoneEvent)
            bulkSave += currentDomains
        }
        
        trafficZoneStatsMonitorRepository.saveAll(bulkSave)
    }

    /**
     * @param currentDomains   : zone 별 현재 시간대 (분, 시간, 일) 에 대해 조회된 리스트
     * @param zoneEvent        : zone 별 이벤트로 전달받은 값 (시간대 정보 없음)
     *      zoneEvent 의 entryCountPerDay 는 일 별 초기화되므로 시간대에 따라 차등 계산한다.
     *      zoneEvent 의 maxThreshold, maxWaitingCount, maxEstimatedClearTime 은 일 별 우상향 값이 아닌 실시간 값이므로 최대값만 적용.
     * 월 집계는 (해당 시간대의 첫 데이터 ~ 현재 시간대의 직전 데이터) 까지의 값을 실시간으로 지속 조회해야 되는 오버헤드로 인해 월 집계는 하지 않는다.
     * totalEntryCount 는 우선 일 별로 집계하되, 통계 조회 시 각 시간대에 맞게 연산한다.
     */
    private fun updateDomainMetrics(
        currentDomains: List<TrafficZoneStatsMonitor>,
        zoneEvent: TrafficZoneStatsMonitor
    ): List<TrafficZoneStatsMonitor> {
        currentDomains.forEach { currentDomain ->
            currentDomain.apply {
                currentDomain.totalEntryCount = zoneEvent.entryCountPerDay
                if (currentDomain.maxThreshold < zoneEvent.threshold) currentDomain.maxThreshold = zoneEvent.threshold
                if (currentDomain.maxWaitingCount < zoneEvent.waitingCount) currentDomain.maxWaitingCount = zoneEvent.waitingCount
                if (currentDomain.maxEstimatedClearTime < zoneEvent.estimatedClearTime) currentDomain.maxEstimatedClearTime = zoneEvent.estimatedClearTime
            }
        }

        return currentDomains
    }

    private fun statsDateStrings(now: LocalDateTime, plusUnit: Long = 0): List<String> {
        return listOf(
            DATE_BASIC_PATTERN,
            DATE_TIME_PATTERN_yyyyMMddHH,
            DATE_TIME_PATTERN_yyyyMMddHHmm
        ).map { now.convertPatternAndAddUnitOf(it, plusUnit) }
    }

    private suspend fun findOrCreateMissingStatsMonitors(
            zoneId: String, statsDateStrings: List<String>, zoneAlias: String
    ): List<TrafficZoneStatsMonitor> {

        val entityIds = statsDateStrings.map { statsDate ->
            TrafficZoneStatsMonitorId(zoneId = zoneId, statsDate = statsDate)
        }
        val existingMonitors = trafficZoneStatsMonitorRepository.findAllByIdIn(entityIds)
        val existingMonitorsMap = existingMonitors.associateBy { it.statsDate }

        val allMonitors = mutableListOf<TrafficZoneStatsMonitor>()
        statsDateStrings.forEach { statsDate ->
            val monitor = existingMonitorsMap[statsDate] ?: TrafficZoneStatsMonitor(
                zoneId = zoneId,
                zoneAlias = zoneAlias,
            ).apply {
                this.statsDate = statsDate
                this.statsType = determineStatsTypeFromDate(statsDate)
            }
            allMonitors.add(monitor)
        }

        return allMonitors
    }

    fun determineStatsTypeFromDate(dateString: String): StatsType {
        return when(dateString.length) {
            8 -> StatsType.DAY
            10 -> StatsType.HOUR
            12 -> StatsType.MINUTE
            else -> StatsType.MINUTE
        }
    }

    suspend fun oneUnitBeforeStatsDate(statsDate: String, statsType: StatsType): String {
        return when(statsType) {
            StatsType.MINUTE -> statsDate.convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHHmm).convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, -1)
            StatsType.HOUR -> statsDate.convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHH).convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHH, -1)
            StatsType.DAY -> (statsDate + "00").convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHH).convertPatternAndAddUnitOf(DATE_BASIC_PATTERN, -1)
            StatsType.MONTH -> (statsDate + "0100").convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHH).convertPatternAndAddUnitOf(DATE_PATTERN_yyyyMM, -1)
        }
    }
}
