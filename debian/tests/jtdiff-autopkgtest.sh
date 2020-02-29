#!/bin/bash
set -o errexit
set -o errtrace
set -o pipefail
set -o nounset

testsuite=$1
shift

if [ -z "${AUTOPKGTEST_TMP+x}" ] || [ -z "${AUTOPKGTEST_ARTIFACTS+x}" ]; then
  echo "Environment variables AUTOPKGTEST_TMP and AUTOPKGTEST_ARTIFACTS must be set" >&2
  exit 1
fi

host_arch=${DEB_HOST_ARCH:-$(dpkg --print-architecture)}

vmname=${VMNAME:-hotspot}

jt_report_tb="/usr/share/doc/openjdk-7-jre-headless//test-${host_arch}/jtreport-${vmname}.tar.xz.gz"
jt_report_tb_="${AUTOPKGTEST_TMP}/jtreport-${vmname}.tar.xz"

if [ ! -f "${jt_report_tb}" ]; then
  echo "Unable to compare jtreg results: no build jtreport found for ${vmname}/${host_arch}."
  echo "Reason: '${jt_report_tb}' does not exist."
  exit 77
fi

# create directories to hold the results
mkdir -p "${AUTOPKGTEST_ARTIFACTS}/${testsuite}"
mkdir -p "${AUTOPKGTEST_TMP}/openjdk-pkg-jtreg-report"

current_report_dir="${AUTOPKGTEST_ARTIFACTS}/jtreg-hotspot/${testsuite}"
previous_report_dir="${AUTOPKGTEST_TMP}/openjdk-pkg-jtreg-report/jtreg-hotspot/${testsuite}"

# extract testsuite results from openjdk package
[ -d "${previous_report_dir}" ] || \
  gzip -cd "${jt_report_tb}" > "${jt_report_tb_}"
  tar -Jxf "${jt_report_tb_}" --strip-components=2 -C "${AUTOPKGTEST_TMP}/openjdk-pkg-jtreg-report"


jtdiff -o "${current_report_dir}/jtdiff.html" "${previous_report_dir}/JTreport" "${current_report_dir}/JTreport" || true
jtdiff "${previous_report_dir}/JTreport" "${current_report_dir}/JTreport" | tee "${current_report_dir}/jtdiff.txt" || true

# create jdiff super-diff structure
jtdiff_dir="${AUTOPKGTEST_TMP}/jtdiff-${testsuite}/${host_arch}"
mkdir -p "${jtdiff_dir}/"{1,2} "${current_report_dir}/jtdiff-super"
ln -sf "${previous_report_dir}/JTreport" "${jtdiff_dir}/1/"
ln -sf "${current_report_dir}/JTreport" "${jtdiff_dir}/2/"

# run jtdiff super-diff
jtdiff -o "${current_report_dir}/jtdiff-super/" -s "${AUTOPKGTEST_TMP}/jtdiff-${testsuite}/" || true

# fail if we detect a regression
if egrep '^(pass|---) +(fail|error)' "${current_report_dir}/jtdiff.txt"; then exit 1; else exit 0; fi
