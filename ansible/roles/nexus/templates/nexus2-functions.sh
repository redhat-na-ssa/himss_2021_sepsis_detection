#################################################################
# Functions for Managing Sonatype Nexus                         #
#                                                               #
# Authors:                                                      #
# - Jorge Morales        https://github.com/jorgemoralespou     #
# - Siamak Sadeghianfar  https://github.com/siamaksade          #
#                                                               #
#################################################################

#
# add_nexus2_repo [repo-id] [repo-url] [nexus-username] [nexus-password] [nexus-url]
#

function add_nexus2_repo() {
  local _REPO_ID=$1
  local _REPO_URL=$2
  local _NEXUS_USER=$3
  local _NEXUS_PWD=$4
  local _NEXUS_URL=$5

  read -r -d '' _REPO_JSON << EOM
{
   "data": {
      "repoType": "proxy",
      "id": "$_REPO_ID",
      "name": "$_REPO_ID",
      "browseable": true,
      "indexable": true,
      "notFoundCacheTTL": 1440,
      "artifactMaxAge": -1,
      "metadataMaxAge": 1440,
      "itemMaxAge": 1440,
      "repoPolicy": "RELEASE",
      "provider": "maven2",
      "providerRole": "org.sonatype.nexus.proxy.repository.Repository",
      "downloadRemoteIndexes": false,
      "autoBlockActive": true,
      "fileTypeValidation": true,
      "exposed": true,
      "checksumPolicy": "WARN",
      "remoteStorage": {
         "remoteStorageUrl": "$_REPO_URL",
         "authentication": null,
         "connectionSettings": null
      }
   }
}
EOM

  curl -v -f -X POST -H "Accept: application/json" -H "Content-Type: application/json" -d "$_REPO_JSON" -u "$_NEXUS_USER:$_NEXUS_PWD" "$_NEXUS_URL/service/local/repositories"

}

#
# set_nexus2_repo_write_policy [repo-id] [current-policy] [new-policy] [nexus-username] [nexus-password] [nexus-url]
#
function set_nexus2_repo_write_policy() {
  local _REPO_ID=$1
  local _POLICY_ID=$2
  local _NEW_POLICY_ID=$3
  local _NEXUS_USER=$4
  local _NEXUS_PWD=$5
  local _NEXUS_URL=$6

  curl $_NEXUS_URL/service/local/repositories/$_REPO_ID | sed "s/$_POLICY_ID/$_NEW_POLICY_ID/g" | curl -X PUT -u "$_NEXUS_USER:$_NEXUS_PWD"  $_NEXUS_URL/service/local/repositories/$_REPO_ID -i -H 'Content-Type: application/xml' -d @-
}

#
# add_nexus2_group_repo [repo-id] [group-id] [nexus-username] [nexus-password] [nexus-url]
#
function add_nexus2_group_repo() {
  local _REPO_ID=$1
  local _GROUP_ID=$2
  local _NEXUS_USER=$3
  local _NEXUS_PWD=$4
  local _NEXUS_URL=$5

  GROUP_JSON=$(curl -s -H "Accept: application/json" -H "Content-Type: application/json" -f -X GET -u "$_NEXUS_USER:$_NEXUS_PWD" "$_NEXUS_URL/service/local/repo_groups/$_GROUP_ID")
  GROUP_JSON_WITH_REPO=$(echo $GROUP_JSON | sed "s/\"repositories\":\[/\"repositories\":[{\"id\": \"$_REPO_ID\"},/g")
  curl -v -f -X PUT -H "Accept: application/json" -H "Content-Type: application/json" -d "$GROUP_JSON_WITH_REPO" -u "$_NEXUS_USER:$_NEXUS_PWD" "$_NEXUS_URL/service/local/repo_groups/$_GROUP_ID"
}


#
# add_nexus2_redhat_repos [nexus-username] [nexus-password] [nexus-url]
#
function add_nexus2_redhat_repos() {
  local _NEXUS_USER=$1
  local _NEXUS_PWD=$2
  local _NEXUS_URL=$3

  add_nexus2_repo redhat-ga https://maven.repository.redhat.com/ga/ $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL
  add_nexus2_repo redhat-ea https://maven.repository.redhat.com/earlyaccess/all/ $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL
  add_nexus2_repo redhat-techpreview https://maven.repository.redhat.com/techpreview/all $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL
  add_nexus2_repo jboss-ce https://repository.jboss.org/nexus/content/groups/public/ $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL

  add_nexus2_group_repo jboss-ce public $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL
  add_nexus2_group_repo redhat-ea public $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL
  add_nexus2_group_repo redhat-techpreview public $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL
  add_nexus2_group_repo redhat-ga public $_NEXUS_USER $_NEXUS_PWD $_NEXUS_URL
}
