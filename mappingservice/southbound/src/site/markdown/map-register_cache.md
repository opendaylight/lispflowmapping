#Performance - Use a local cache for Map-Registers in byte[] form in SB, and only update map-cache if necessary

##Feature overview
xTR devices sent periodical updates through map register message which are often the same

Store incomming map register messages in local cache to avoid deserialization of the same message.

##Implementation
###Hash map

Cache is implemented in class +MapRegisterCache+ as hash map. Key of this hash map is instance of *MapRegisterCacheKey* and value is instance of *MapRegisterCacheValue*.

After receiving map register message it is firstly checked whether this message has been received before. If it so and is still valid (is in cache without refresh less then 90 seconds) then it isn't deserialized. Otherway it comletely deserialized.

To be able to say that received map register message is the same as other previously received there is key (instance of +MapRegisterCacheKey+) which holds following values:

###Key

* **eidPrefix** - array of bytes which comes from first record in map register message. In [RFC 6830](https://tools.ietf.org/html/rfc6830#page-38) it is named *EID-Prefix*. If EID-Prefix-AFI (field just infront of EID-Prefix) has value 16387 then EID-Prefix contains also instance identifier.
* **xTRId** - array of bytes (16 B) which comes from the end of map register message according to [Lisp-NAT-Traversal](https://tools.ietf.org/html/draft-ermagan-lisp-nat-traversal-10#page-10) draft,
* **siteId** - arrays of bytes (8 B) which comes from the end of map register message according to [Lisp-NAT-Traversal](https://tools.ietf.org/html/draft-ermagan-lisp-nat-traversal-10#page-10) draft.

Combination of this 3 values should be enough to uniquely identify each map register message.

###Value

Value part of hash map record is created in two phases:

1. during partional deserialization:
 * **val** - byte array which contains map register in the same state as was received
 * **wantMapNotify** - boolean value which is set according to value of M bib (24th bit of map register message)
 * **mergeBit** - boolean value which is set according to value of 22th bit in map register message
1. during full deserialization:
 * **list of EIDs** - all EIDS (transformed value of EID-Prefix) for all records,
 * **xTRId** - instance of +XtrId+ class,
 * **siteId** - instance of +SiteId+ class,
 * **timestamp** - when was this value object created o last refreshed.

##New handling of map register message
1. partial deserialization is done - result of this operation is map entry object which consists of pair - key and value
1. if map entry key is in cache and is still valid
 * new type of notification mapping-keep-alive is sent (it is caught in +LispMappingService+)
 * if want-map-notify bit is set then also map-notify message is sent
 * map entry value is refreshed - timestamp is set to current time
 * done
1. if map entry key is in cache but isn't valid - entry is removed from cache and continue with following step
1. if map entry isn't in cache
 * message is processed as before (complete deserialization)
 * some data from deserialization are stored to map entry value
 * timestamp for map entry value is set to current time
 * done

##Consideration
* It isn't necessary to store mergeBit in value part of hash map record because only records which have merge bit set to value 0 are stored to hash map

##Results comparison
Performance test 010_Southbound_MapRequest.robot from integration-test/csit/suites/lispflowmapping/performance was ran 3 times with master build and 3 times with patch build. Results below:
<table border="true">
<tr>
	<th>run</th>
	<th>replies/s</th>
	<th>notifies/s</th>
	<th>store/s</th>
</tr>
<tr>
	<td>master 1</td>
	<td>25528.03</td>
	<td>3207.33792662</td>
	<td>335.244225418</td>
</tr>
<tr>
	<td>master 2</td>
	<td>26270.7</td>
	<td>3427.32</td>
	<td>323.070461668</td>
</tr>
<tr>
	<td>master 3</td>
	<td>26151.48</td>
	<td>3373.95</td>
	<td>321.998969603</td>
</tr>
<tr>
	<td>patch 1</td>
	<td>25170.54</td>
	<td>100.0</td>
	<td>295.333727112</td>
</tr>
<tr>
	<td>patch 2</td>
	<td>25976.76</td>
	<td>99.99400036</td>
	<td>322.841000807</td>
</tr>
<tr>
	<td>patch 3</td>
	<td>26930.6</td>
	<td>100.0</td>
	<td>328.7635204</td>
</tr>
</table>

