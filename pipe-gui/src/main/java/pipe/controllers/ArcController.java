package pipe.controllers;

import pipe.historyActions.*;
import pipe.models.component.arc.Arc;
import pipe.models.component.arc.ArcPoint;
import pipe.models.component.Connectable;
import pipe.models.component.token.Token;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class ArcController<S extends Connectable, T extends Connectable> extends AbstractPetriNetComponentController<Arc<S, T>>
{
    private final Arc<S, T> arc;
    private final HistoryManager historyManager;

    ArcController(Arc<S, T> arc, HistoryManager historyManager) {

        super(arc, historyManager);
        this.arc = arc;
        this.historyManager = historyManager;
    }

    /**
     * Sets the weight for the current arc
     * @param token
     * @param expr
     */
    public void setWeight(Token token, String expr) {
        historyManager.newEdit();
        updateWeightForArc(token, expr);
    }

    /**
     * Creates a historyItem for updating weight and applies it
     * @param token token to associate the expression with
     * @param expr new weight expression for the arc
     */
    private void updateWeightForArc(Token token,
                                    String expr) {
        String oldWeight = arc.getWeightForToken(token);
        arc.setWeight(token, expr);

        SetArcWeightAction<S,T> weightAction = new SetArcWeightAction<>(arc, token, oldWeight, expr);
        historyManager.addEdit(weightAction);
    }

    public void setWeights(Map<Token, String> newWeights) {
        HashMap<Token, String> previousRates = new HashMap<>(arc.getTokenWeights());

        historyManager.newEdit();
        for (Map.Entry<Token, String> entry : newWeights.entrySet()) {
            updateWeightForArc(entry.getKey(), entry.getValue());
        }
    }

    public String getWeightForToken(Token token) {
        return arc.getWeightForToken(token);
    }

    public boolean hasFunctionalWeight() {
        return arc.hasFunctionalWeight();
    }

    public Connectable getTarget() {
        return arc.getTarget();
    }

    public void toggleArcPointType(ArcPoint arcPoint) {
        HistoryItem historyItem = new ArcPathPointType(arcPoint);
        historyItem.redo();
        historyManager.addNewEdit(historyItem);
    }

    public void splitArcPoint(final ArcPoint arcPoint) {
        ArcPoint nextPoint = arc.getNextPoint(arcPoint);

        double x = (arcPoint.getPoint().getX() + nextPoint.getPoint().getX())/2;
        double y = (arcPoint.getPoint().getY() + nextPoint.getPoint().getY())/2;

        Point2D point = new Point2D.Double(x,y);
        ArcPoint newPoint = new ArcPoint(point, arcPoint.isCurved());
        HistoryItem historyItem = new AddArcPathPoint<S,T>(arc, newPoint);
        historyItem.redo();
        historyManager.addNewEdit(historyItem);
    }

    public void addPoint(Point2D point) {
        ArcPoint newPoint = new ArcPoint(point, false);
        HistoryItem historyItem = new AddArcPathPoint<S,T>(arc, newPoint);
        historyItem.redo();
        historyManager.addNewEdit(historyItem);
    }

    public void deletePoint(ArcPoint component) {
        HistoryItem historyItem = new DeleteArcPathPoint<S,T>(arc, component);
        historyItem.redo();
        historyManager.addNewEdit(historyItem);
    }
}
