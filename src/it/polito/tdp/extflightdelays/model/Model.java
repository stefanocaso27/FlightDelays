package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	Map<Integer, Airport> aIdMap;
	Map <Airport, Airport> visita;
	
	public Model() {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		aIdMap = new HashMap<Integer, Airport>();
		visita = new HashMap<Airport, Airport>();
	}
	
	public void creaGrafo(int distanzaMedia) {
		ExtFlightDelaysDAO dao = new ExtFlightDelaysDAO();
		dao.loadAllAirports(aIdMap);
		
		//aggiungere i vertici
		Graphs.addAllVertices(grafo, aIdMap.values());
		
		for(Rotta rotta : dao.getRotte(aIdMap, distanzaMedia)) {
			//1 - controllo se esiste gia' un arco
			//2 - se esiste, aggiorno il peso
			
			DefaultWeightedEdge edge = grafo.getEdge(rotta.getPartenza(), rotta.getDestinazione());
			if(edge == null) {
				Graphs.addEdge(grafo, rotta.getPartenza(), rotta.getDestinazione(), rotta.getDistanceMedia());
			} else {
				double peso = grafo.getEdgeWeight(edge);
				double newPeso = (peso + rotta.getDistanceMedia())/2;
				System.out.println("Aggiornare peso!");
				System.out.println("Peso vecchio: " + peso + " peso nuovo: " + newPeso);
				
				grafo.setEdgeWeight(edge, newPeso);		
			}	
		}
		
		System.out.println("Grafo creato!");
		System.out.println("Vertici: " + grafo.vertexSet().size());
		System.out.println("Archi: " + grafo.edgeSet().size());
	}
	
	public Boolean testConnessione(Integer a1, Integer a2) {
		Set<Airport> visitati = new HashSet<Airport>();
		
		Airport partenza = aIdMap.get(a1);
		Airport destinazione = aIdMap.get(a2);
		System.out.println("Testo connessione tra " + partenza + " e " + destinazione);
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, partenza);
		
		while (it.hasNext())
			visitati.add(it.next());
		
		if(visitati.contains(destinazione))
			return true;
		else
			return false;
	}
	
	public List<Airport> trovaPercorso(Integer a1, Integer a2) {
		List<Airport> percorso = new ArrayList<Airport>();
		
		Airport partenza = aIdMap.get(a1);
		Airport destinazione = aIdMap.get(a2);
		System.out.println("Cerco percorso tra " + partenza + " e " + destinazione);
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, partenza);
		
		visita.put(partenza, null);
		
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
				Airport sorgente = grafo.getEdgeSource(ev.getEdge());
				Airport destinazione = grafo.getEdgeTarget(ev.getEdge());
				
				if(!visita.containsKey(destinazione) && visita.containsKey(sorgente)) {
					visita.put(destinazione, sorgente);
				} else if(visita.containsKey(destinazione) && !visita.containsKey(sorgente)) {
					visita.put(sorgente, destinazione);
				}
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		while (it.hasNext())
			it.next();
		
		if(!visita.containsKey(partenza) || !visita.containsKey(destinazione)) {
			return null;
		}
		
		Airport step = destinazione;
		while(!step.equals(partenza)) {
			percorso.add(step);
			step = visita.get(step);
		}
		percorso.add(step);
		
		return percorso;
	}
	
}


