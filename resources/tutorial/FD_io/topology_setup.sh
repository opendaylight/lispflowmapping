#!/usr/bin/env bash
# path to vpp executable and configurations folder
VPP_LITE_BIN=/vpp/build-root/install-vpp_lite_debug-native/vpp/bin/vpp
VPP_LITE_CONF=/etc/vpp/lite/

pkill vpp

# delete previous incarnations if they exist
ip netns exec xtr-rtr-ns ifconfig vppbr1 down
ip netns exec xtr-rtr-ns brctl delbr vppbr1
ip link del dev vpp1 &> /dev/null
ip link del dev vpp2 &> /dev/null
ip link del dev xtr_rtr1 &> /dev/null
ip link del dev xtr_rtr2 &> /dev/null
ip link del dev xtr_rtr3 &> /dev/null
ip link del dev odl &> /dev/null

ip netns del vpp-ns1 &> /dev/null
ip netns del vpp-ns2 &> /dev/null
ip netns del xtr-rtr-ns &> /dev/null

if [ "$1" == "clean" ] ; then
  exit 0
fi

if [ ! -e ${VPP_LITE_BIN} ] ; then
  echo "VPP binary not found: $VPP_LITE_BIN"
  exit 1
fi

ip netns add vpp-ns1
ip netns add vpp-ns2
ip netns add xtr-rtr-ns

ip link add veth_xtr_rtr1 type veth peer name xtr_rtr1
ip link add veth_xtr_rtr2 type veth peer name xtr_rtr2
ip link add veth_xtr_rtr3 type veth peer name xtr_rtr3
ip link add veth_odl type veth peer name odl
ip link set dev xtr_rtr1 up
ip link set dev xtr_rtr2 up
ip link set dev xtr_rtr3 up
ip link set dev odl up

ip link set dev veth_xtr_rtr1 up netns xtr-rtr-ns
ip link set dev veth_xtr_rtr2 up netns xtr-rtr-ns
ip link set dev veth_xtr_rtr3 up netns xtr-rtr-ns
ip link set dev veth_odl up netns xtr-rtr-ns

ip netns exec xtr-rtr-ns brctl addbr vppbr1
ip netns exec xtr-rtr-ns brctl addif vppbr1 veth_xtr_rtr1
ip netns exec xtr-rtr-ns brctl addif vppbr1 veth_xtr_rtr2
ip netns exec xtr-rtr-ns brctl addif vppbr1 veth_xtr_rtr3
ip netns exec xtr-rtr-ns brctl addif vppbr1 veth_odl
ip netns exec xtr-rtr-ns ifconfig vppbr1 up

ip link add veth_vpp1 type veth peer name vpp1
ip link set dev vpp1 up
ip link set dev veth_vpp1 up netns vpp-ns1

ip netns exec vpp-ns1 \
  bash -c "
    ip link set dev lo up
    ip addr add 6.0.2.2/24 dev veth_vpp1
    ip route add 6.0.4.0/24 via 6.0.2.1
"

ip link add veth_vpp2 type veth peer name vpp2
ip link set dev vpp2 up
ip link set dev veth_vpp2 up netns vpp-ns2

ip netns exec vpp-ns2 \
  bash -c "
    ip link set dev lo up
    ip addr add 6.0.4.4/24 dev veth_vpp2
    ip route add 6.0.2.0/24 via 6.0.4.1
"

ifconfig odl 6.0.3.100/24
ethtool --offload  odl rx off tx off

sudo ${VPP_LITE_BIN} \
  unix { log /tmp/vpp1.log cli-listen \
         localhost:5002 full-coredump \
         exec ${VPP_LITE_CONF}/vpp1.config } \
  api-trace { on } api-segment {prefix xtr1}

sudo ${VPP_LITE_BIN} \
  unix { log /tmp/vpp2.log cli-listen \
         localhost:5003 full-coredump \
         exec ${VPP_LITE_CONF}/vpp2.config } \
  api-trace { on } api-segment {prefix xtr2}

sudo ${VPP_LITE_BIN} \
  unix { log /tmp/rtr.log cli-listen \
         localhost:5004 full-coredump \
         exec ${VPP_LITE_CONF}/rtr.config } \
  api-trace { on } api-segment {prefix rtr}
