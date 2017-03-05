/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.mutationhotspots.mutationhotspotsdetection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.cbioportal.mutationhotspots.mutationhotspotsdetection.impl.MutatedResidueImpl;
import org.cbioportal.mutationhotspots.mutationhotspotsdetection.impl.ProteinStructureHotspotDetective;
import org.cbioportal.mutationhotspots.mutationhotspotsdetection.utils.EnsemblUtils;
import org.cbioportal.mutationhotspots.mutationhotspotsdetection.utils.MafReader;

/**
 *
 * @author jgao
 */
public class HotspotMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, HotspotException {
        Set<String> mutationTypeFilter = Collections.singleton("Missense_Mutation");
        
        InputStream isFa = HotspotMain.class.getResourceAsStream("/data/Homo_sapiens.GRCh38.pep.all.fa");
        Map<String, Protein> proteins = EnsemblUtils.readFasta(isFa);
        
        InputStream isMaf = HotspotMain.class.getResourceAsStream("/data/MAP2K1.maf");
        
        Collection<MutatedProtein> mutatedProteins = MafReader.readMaf(isMaf, mutationTypeFilter, proteins);
        
        HotspotDetectiveParameters params = HotspotDetectiveParameters.getDefaultHotspotDetectiveParameters();
        params.setIdentpThresholdFor3DHotspots(100.0);
        params.setPValueThreshold(1.0);
        
        HotspotDetective hd = new ProteinStructureHotspotDetective(params);
                
        int idHotspot = 0;
        for (MutatedProtein mutatedProtein : mutatedProteins) {
            Set<Hotspot> hotspots = hd.detectHotspots(mutatedProtein);
            for (Hotspot hotspot : hotspots) {
                hotspot.setId(++idHotspot);
                System.out.println(hotspot.getLabel());
            }
            
            Collection<MutatedResidue> mutatedResidues = mutatedResiduesOnAProtein(mutatedProtein, hotspots);
            mutatedResidues.forEach((mutatedResidue) -> {System.out.println(mutatedResidue);});
            
        }
    }
    
    private static Collection<MutatedResidue> mutatedResiduesOnAProtein(MutatedProtein mutatedProtein, Set<Hotspot> hotspots) {
        SortedMap<Integer, MutatedResidue> map = new TreeMap<>();
        for(Hotspot hs : hotspots) {
            Set<Integer> residues = hs.getResidues();
            for (Integer r : residues) {
                MutatedResidue mutatedResidue = map.get(r);
                if (mutatedResidue==null) {
                    mutatedResidue = new MutatedResidueImpl(mutatedProtein, r);
                    map.put(r, mutatedResidue);
                }
                mutatedResidue.addHotspot(hs);
            }
        }
        return map.values();
    }
}
