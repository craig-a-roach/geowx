AUTHOR('craig');
PURPOSE('Unit Test');

var lmBase = Number('20110601T1200Z00M000');

var lslA = [
{name: 'fh.0000_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase},
{name: 'fh.0000_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 1m},
{name: 'fh.0000_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 1m},
{name: 'fh.0000_pa.membrp03_tl.press_gr.onedeg', lastModified: lmBase + 1m},
{name: 'fh.0006_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 2m},	
{name: 'fh.XXXX_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 2m},	
{name: 'fh.0006_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 2m},
{name: 'fh.0006_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 2m},
{name: 'fh.0012_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0006_pa.membrp03_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0006_pa.membrp04_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0012_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 5m},
{name: 'fh.0012_pa.membrp03_tl.press_gr.onedeg', lastModified: lmBase + 5m},
{name: 'fh.0012_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 5m},
{name: 'fh.0012_pa.membrpXX_tl.press_gr.onedeg', lastModified: lmBase + 5m},		
{name: 'fh.0018_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 7m},
{name: 'fh.0036_pa.membrc00_tl.press_gr.onedeg', lastModified: lmBase + 8m},
{name: 'fh.0036_pa.membrp01_tl.press_gr.onedeg', lastModified: lmBase + 8m},
{name: 'fh.0036_pa.membrp02_tl.press_gr.onedeg', lastModified: lmBase + 8m},
{name: 'fh.0036_pa.membrp03_tl.press_gr.onedeg', lastModified: lmBase + 8m}
];

var lslB = [
{name: 'fh.0000_tl.press_gr.onedeg', lastModified: lmBase},
{name: 'fh.0012_tl.press_gr.onedeg', lastModified: lmBase + 3m},
{name: 'fh.0006_tl.press_gr.onedeg', lastModified: lmBase + 1m}
];

var lslC = [
{name: 'tl.press_gr.onedeg', lastModified: lmBase}
];

var ftpA = {
	gridFilePattern: 'fh[.](\\d\\d\\d\\d)_pa[.]membr(c00|p\\d\\d)_tl[.]press_gr[.]onedeg'	
};

var ftpB = {
		gridFilePattern: 'fh[.](\\d\\d\\d\\d)_tl[.]press_gr[.]onedeg'	
	};

var ftpC = {
		gridFilePattern: 'tl[.]press_gr[.]onedeg'	
	};


var productionA = {
	progStep: 6h,
	members: ['c00','p03','p01','p02']
};

var productionB = {
		progStep: 6h
	};

var productionC = {
	};


function format(pg) {
	var t = '';
	for (var i=0, b; i < pg.length; i++) {
		b = pg[i];
		t += '['+i;
		if (b.prognosis) {
			t += ' prog '+ b.prognosis;
		}
		t += ' lastModified '+b.lastModified;
		t += ' files '+b.nameArray[0]+'...'+b.nameArray.length;
		if (b.memberArray) {
			t += ' members '+b.memberArray.join();
		}
		t += ']\n';
	}
	return t;
}


var pgA1 = parseGridBlocks(lslA, 0h, ftpA, productionA);
var pgAs1 = format(pgA1);
var pgA2 = parseGridBlocks(lslA, 12h, ftpA, productionA);
var pgAs2 = format(pgA2);
var pgA3 = parseGridBlocks(lslA, 18h, ftpA, productionA);
var pgAs3 = format(pgA3);

var pgB1 = parseGridBlocks(lslB, 0h, ftpB, productionB);
var pgBs1 = format(pgB1);
var pgB2 = parseGridBlocks(lslB, 12h, ftpB, productionB);
var pgBs2 = format(pgB2);

var pgC1 = parseGridBlocks(lslC, 0h, ftpC, productionC);
var pgCs1 = format(pgC1);


function parseGridBlocks(lsl, nextPrognosis, ftp, pd) {
	var gridFilePattern = RegExp(ftp.gridFilePattern);
	var memberMask = pd.members ? pd.members.slice().sort().join('|') : '';
	var blockArray = [];
	var i,e,fca,fc,g1,g2,prog,iprog,block;
	for (i=0; i < lsl.length; i++) {
		e = lsl[i];
		fca = gridFilePattern.capture(e.name);
		if (fca.length == 0) continue;
		fc = fca[0];
		if (fc.length < 2) {
			blockArray[0] = {lastModified: e.lastModified, nameArray:[e.name]};
			break;
		}
		g1 = Number(fc[1]);
		if (!g1) continue;
		prog = g1 * 1h;
		if (prog < nextPrognosis) continue;
		iprog = (prog - nextPrognosis) / pd.progStep;
		if (fc.length == 2) {
			blockArray[iprog] = {prognosis: prog, lastModified: e.lastModified, nameArray:[e.name]};
			continue;
		}
		g2 = fc[2];
		block = blockArray[iprog];
		if (!block) {
			block = {prognosis: prog, lastModified: null, nameArray: [], memberArray:[]};
			blockArray[iprog] = block;
		}
		block.nameArray.push(e.name);
		block.memberArray.push(g2);
		block.lastModified = Math.max(block.lastModified, e.lastModified);
	}
	
	var blockMask;
	var result = [];
	for (i=0; i < blockArray.length; i++) {
		block = blockArray[i];
		if (!block) break;
		block.nameArray.sort();
		if (block.memberArray) {
			block.memberArray.sort();
			blockMask = block.memberArray.join('|');
			if (!blockMask.startsWith(memberMask)) break;
		}
		result.push(block);
	}
	return result;
}