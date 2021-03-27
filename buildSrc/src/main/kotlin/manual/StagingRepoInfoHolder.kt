package manual

object StagingRepoInfoHolder {

    private var stagedRepoInfo: StagedRepoInfo? = null

    fun setStagedRepoInfo(stagedRepoInfo: StagedRepoInfo) {
        this.stagedRepoInfo = stagedRepoInfo
    }

    fun getStageRepoInfo(): StagedRepoInfo {
        check(stagedRepoInfo != null) { "Staged repository info not available!" }
        return stagedRepoInfo!!
    }
}

data class StagedRepoInfo(
    val stagedRepositoryId: String,
    val stagedRepositoryUrl: String,
    val stagedRepositoryDescription: String)