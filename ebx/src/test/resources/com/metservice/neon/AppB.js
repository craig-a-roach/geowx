AUTHOR('craig');
PURPOSE('Unit Test');

var lmBase = Number('20110627T2245Z00M000');

var lslK = [
{name: 'fh.0000_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 0m},
{name: 'fh.0000_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 0m},
{name: 'fh.0000_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 0m},
{name: 'fh.0006_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 1m},
{name: 'fh.0006_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 1m},
{name: 'fh.0006_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 1m},
{name: 'fh.0012_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 2m},
{name: 'fh.0012_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 1m},
{name: 'fh.0012_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 2m},
{name: 'fh.0018_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 2m},
{name: 'fh.0018_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 2m},
{name: 'fh.0018_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 2m},
{name: 'fh.0024_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0024_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0024_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0030_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 4m},
{name: 'fh.0030_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0030_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 3m}
];

var a = newPrognosisControl({prognosisStep: 6h, prognosisLimit: 18h});
Shell.trace('a='+new JsonEncoder(a));
var a1 = nextPrognosis(0h, a);
var a2 = nextPrognosis(6h, a);
var a3 = nextPrognosis(12h, a);
var a4 = nextPrognosis(18h, a);
var a10 = _prognosisRelativeIndex(0h,  0h, a); //0
var a11 = _prognosisRelativeIndex(0h,  6h, a); //1
var a12 = _prognosisRelativeIndex(0h, 12h, a); //2
var a13 = _prognosisRelativeIndex(0h, 18h, a); //3
var a14 = _prognosisRelativeIndex(0h, 24h, a); //4

var b = newPrognosisControl({prognosisSteps: [{step:3h, upto:12h},{step:6h}], prognosisLimit:24h});
var b1 = nextPrognosis(0h, b);
var b2 = nextPrognosis(3h, b);
var b3 = nextPrognosis(6h, b);
var b4 = nextPrognosis(9h, b);
var b5 = nextPrognosis(12h, b);
var b6 = nextPrognosis(18h, b);
var b7 = nextPrognosis(24h, b);

var c = newPrognosisControl({prognosisSteps: [{step:3h, upto:12h},{step:6h, upto:24h}, {step:12h}], prognosisLimit:48h});
var c1 = _prognosisRelativeIndex(0h, 36h, c); //7
var c2 = _prognosisRelativeIndex(3h, 36h, c); //6
var c3 = _prognosisRelativeIndex(6h, 36h, c); //5
var c4 = _prognosisRelativeIndex(9h, 36h, c); //4
var c5 = _prognosisRelativeIndex(12h, 36h, c); //3
var c6 = _prognosisRelativeIndex(18h, 36h, c); //2
var c7 = _prognosisRelativeIndex(24h, 36h, c); //1
var c8 = _prognosisRelativeIndex(36h, 36h, c); //0
var c9 = _prognosisRelativeIndex(36h, 48h, c); //1
var c10= _prognosisRelativeIndex(48h, 48h, c); //0
var c11= _prognosisRelativeIndex(60h, 48h, c); //0

var k = {
		ctl: newPrognosisControl({prognosisStep: 6h, prognosisLimit: 18h}),
		members: ['c00','p01','p02'],
		gridFilePattern: /fh[.](\\d\\d\\d\\d)_pa[.]membr(c00|p\\d\\d)_tl[.]press_gr[.]onedeg/,
		prognosisUnit: 1h
		};
var k1 = newPrognosisSnapshot(lslK, 0h, k);
var k1s = k1.extent.prognosisFirst + ' ' + k1.extent.prognosisLast + ' ' + k1.extent.prognosisNext;

function newPrognosisControl(src) {
    var psa = src.prognosisSteps;
    var segments = [];
    var blockCount = 1;
    var origin = 0h;
    var i,seg,gstep,gupto,cgupto;
    if (psa) {
            if (!psa.length) throw 'Expecting an prognosis steps array';
            if (psa.length == 0) throw 'Empty prognosis step array';
            for (i=0; i < psa.length; i++) {
                    seg = psa[i];
                    if (!isElapsed(seg.step)) throw 'Incomplete prognosis step section '+i+'; no elapsed step';
                    if (seg.step == 0h) throw 'Invalid prognosis step section '+i+'; zero step';
                    gstep = seg.step;
                    gupto = seg.upto ? seg.upto : src.prognosisLimit;
                    if (!isElapsed(gupto)) throw 'Incomplete prognosis step section '+i+'; no elapsed upto';
                    cgupto = Math.step(gupto, gstep);
                    blockCount+= ((cgupto - origin) / gstep);
                    segments.push({step: gstep, upto: cgupto});
                    origin = cgupto;
            }
    } else {
            if (!isElapsed(src.prognosisStep)) throw 'Missing prognosis step';
            if (!isElapsed(src.prognosisLimit)) throw 'Missing prognosis limit';
            if (src.prognosisStep == 0h) throw 'Invalid prognosis step; zero';
            gstep = src.prognosisStep;
            gupto = Math.step(src.prognosisLimit, gstep);
            blockCount+= (gupto / src.prognosisStep);
            segments.push({step: gstep, upto: gupto});
    }
    return {segments: segments, blockCount: blockCount};
}

function _extentNextPrognosis(prePrognosis, pctl) {
    var segs = pctl.segments;
    for (var i=0,seg; i < segs.length; i++) {
            seg = segs[i];
            if (prePrognosis < seg.upto) return prePrognosis + seg.step;
    }
    return prePrognosis + segs[-1].step;
}

function _prognosisRelativeIndex(basePrognosis, prognosis, pctl) {
    var segs = pctl.segments;
    var lo = basePrognosis;
    var ri = 0;
    for (var i=0,seg; i < segs.length; i++) {
            seg = segs[i];
            if (lo >= seg.upto) continue;
            if (prognosis < seg.upto) {
                    ri += (prognosis - lo) / seg.step;
                    lo = prognosis;
                    break;
            }
            ri += (seg.upto - lo) / seg.step;
            lo = seg.upto;
    }
    if (lo < prognosis) {
    	ri += (prognosis - lo) / segs[-1].step;
    }
    return ri;
}

function _prognosisLimit(pctl) {
	return pctl.segments[-1].upto;
}

function newPrognosisSnapshot(lsl, prePrognosisNext, pd) {
    var pctl = pd.ctl;
    var prognosisLimit = _prognosisLimit(pctl);
    var memberMask = pd.members.join('|');
    var blockArray = [];
    var i,e,fca,fc,g1,g2,prog,iprog,block;
    for (i=0; i < lsl.length; i++) {
            e = lsl[i];
            fca = pd.gridFilePattern.capture(e.name);
            if (fca.length == 0) continue;
            fc = fca[0];
            if (fc.length < 2) continue;
            g1 = Number(fc[1]);
            if (!g1) continue;
            prog = g1 * pd.prognosisUnit;
            if (prog < prePrognosisNext) continue;
            if (prog > prognosisLimit) continue;
            iprog = _prognosisRelativeIndex(prePrognosisNext, prog, pctl);
            if (fc.length == 2) {
                    blockArray[iprog] = {prognosis: prog, lastModified: e.lastModified, memberArray:[]};
                    continue;
            }
            g2 = fc[2];
            block = blockArray[iprog];
            if (block) {
                    block.memberArray.push(g2);
                    block.lastModified = Math.max(block.lastModified, e.lastModified);
            } else {
                    blockArray[iprog] = {prognosis: prog, lastModified: e.lastModified, memberArray:[g2]};
            }
    }

    var blockMask;
    var rblocks = [];
    var lmmax = null;
    for (i=0; i < blockArray.length; i++) {
            block = blockArray[i];
            if (!block) break;
            blockMask = block.memberArray.sort().join('|');
            if (!blockMask.startsWith(memberMask)) break;
            rblocks.push({prognosis:block.prognosis, lastModified:block.lastModified});
            lmmax = Math.max(lmmax, block.lastModified);
    }
    if (rblocks.length == 0 || !lmmax) return null;
    var rextent = {};
    rextent.prognosisFirst = rblocks[0].prognosis;
    rextent.prognosisLast = rblocks[-1].prognosis;
    rextent.prognosisNext = _extentNextPrognosis(rextent.prognosisLast, pctl);
    rextent.lastModifiedMax = lmmax;
    return {blocks: rblocks, extent: rextent};
}


function isElapsed(e) {
	return (subtypeof e == 'elapsed');
}

function nextPrognosis(prePrognosis, pctl) {
    var segs = pctl.segments;
    for (var i=0,seg; i < segs.length; i++) {
            seg = segs[i];
            if (prePrognosis < seg.upto) return prePrognosis + seg.step;
    }
    return prePrognosis;
}