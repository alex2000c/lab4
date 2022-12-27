package bsu.rfe.java.group6.lab4.Dorozhko.varA6;

import	java.awt.BasicStroke;

import	java.awt.Color;
import	java.awt.Font;
import	java.awt.Graphics;
import	java.awt.Graphics2D;
import	java.awt.Paint;
import java.awt.Shape;
import	java.awt.Stroke;
import	java.awt.font.FontRenderContext;
import	java.awt.geom.Ellipse2D;
import 	java.awt.geom.GeneralPath;
import	java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import	java.awt.geom.Point2D;
import	java.awt.geom.Rectangle2D;
import	javax.swing.JPanel;
@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel
{
	private Double[][] graphicsData;						// Список координат точек для построения графика
	private boolean showAxis = true;						// Флаговые переменные, задающие правила отображения графика
	private boolean showMarkers = true;
	private boolean showGraphics = true;
	
	private double minX;									// Границы диапазона пространства, подлежащего отображению
	private double maxX;
	private double minY;
	private double maxY;
	private double scale;									// Используемый масштаб отображения
	private BasicStroke graphicsStroke;						// Различные стили черчения линий
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;						
	private BasicStroke lineGraphics;
	private Font axisFont;
	private Font myFont00;
	// Различные шрифты отображения надписей
	public GraphicsDisplay() 
	{
		setBackground(Color.WHITE);							// Цвет заднего фона области отображения - белый
		// Сконструировать необходимые объекты, используемые в рисовании:
		
		//BAsicStroke.CAP_BUTT - линия как "обрубок", с обоих сторон
		//BasicStroke.JION_ROUND - полукруглое завершение смыкания линий, BAsicStorke.JOIN_MiTER - смыкание линий "острым шипом"
		
		// Перо для рисования графика - прорисрвываются границы фигур
		//толщина линии 2.0f - две точки, 10.0f - угол смыкания 10 градусов
		graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {10,5,5,5,5,5,5,5,10,5,5,5,10}, 0.0f);
		// Перо для рисования осей координат
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Перо для рисования контуров маркеров
		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Шрифт для подписей осей координат
		axisFont = new Font("Serif", Font.BOLD, 18);
	}
	// Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
	// главного окна приложения в случае успешной загрузки данных
	public void showGraphics(Double[][] graphicsData) 
	{
		// Сохранить массив точек во внутреннем поле класса
		this.graphicsData = graphicsData;
		// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
		repaint();
	}
	
	// Методы-модификаторы для изменения параметров отображения графика
	// Изменение любого параметра приводит к перерисовке области
	public void setShowAxis(boolean showAxis) 	//объявление функций-переменнх для внесения изменений в отображении графика не экране
	{
		this.showAxis = showAxis;				//оси кооординат
		repaint();
	}
	public void setShowMarkers(boolean showMarkers) 
	{
		this.showMarkers = showMarkers;			//точки [закрытые поля данных класса]
		repaint();
	}
	
	public void setShowGraphics(boolean showgraphics)
	{
		this.showGraphics = showGraphics;
		repaint();
	}
	
	/*Для получения экземпляра класса необходимо осуществить приведение к типу Graphics2D экземпляра Graphics,
	получаемого в метод paintComponent() в качестве аргумента*/
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
	
	
	//Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
		if (graphicsData==null || graphicsData.length==0)
			return;
		// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
		// Это необходимо для определения области пространства, подлежащей отображению
		// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
		minX = graphicsData[0][0];
		maxX = graphicsData[graphicsData.length-1][0];
		minY = graphicsData[0][1];
		maxY = minY;
		// Найти минимальное и максимальное значение функции
		for (int i = 1; i<graphicsData.length; i++) 
		{
			if (graphicsData[i][1]<minY) 
			{
				minY = graphicsData[i][1];
			}
			if (graphicsData[i][1]>maxY) 
			{
				maxY = graphicsData[i][1];
			}
		}
/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X и Y -
 *  сколько пикселов приходится на единицу длины по X и по Y (?)
*/
		double scaleX = getSize().getWidth() / (maxX - minX);
		double scaleY = getSize().getHeight() / (maxY - minY);
		// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
		// Выбираем за основу минимальный коэффициент масштаба
		scale = Math.min(scaleX, scaleY);
		// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
		if (scale==scaleX) 
		{
			/* Если за основу был взят масштаб по оси X, значит по оси Y делений меньше,
* т.е. подлежащий визуализации диапазон по Y будет меньше высоты окна. Значит необходимо добавить делений, сделаем это так:
* 1) Вычислим, сколько делений влезет по Y при выбранном масштабе - getSize().getHeight()/scale
* 2) Вычтем из этого сколько делений требовалось изначально
* 3) Набросим по половине недостающего расстояния на maxY и minY*/
			double yIncrement = (getSize().getHeight()/scale - (maxY - minY))/2;
			maxY += yIncrement;
			minY -= yIncrement;
		}
		if (scale==scaleY) 
		{
			// Если за основу был взят масштаб по оси Y, действовать по аналогии
			double xIncrement = (getSize().getWidth()/scale - (maxX - minX))/2;
			maxX += xIncrement;
			minX -= xIncrement;
		}
		// Шаг 7 - Сохранить текущие настройки холста
		Graphics2D canvas = (Graphics2D) g;	
		Stroke oldStroke = canvas.getStroke();	//возвращает текущее перо холста
		Color oldColor = canvas.getColor();		//цвет
		Paint oldPaint = canvas.getPaint();		//заливка
		Font oldFont = canvas.getFont();		//шрифт
		Font myFont00 = new Font("Serif", Font.BOLD, 9);
		
		// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
		// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
		
		// Первыми (если нужно) отрисовываются оси координат.
		if (showAxis)
			paintAxis(canvas);
		
		// Затем отображается сам график
		if (showGraphics)
			paintGraphics(canvas);
				
		// Затем (если нужно) отображаются маркеры точек, по которым строился график.
		if (showMarkers)
			paintMarkers(canvas);
			
		// Шаг 9 - Восстановить старые настройки холста
		canvas.setFont(oldFont);
		canvas.setPaint(oldPaint);
		canvas.setColor(oldColor);
		canvas.setStroke(oldStroke);
	}
	
	// Отрисовка графика по прочитанным координатам
	protected void paintGraphics(Graphics2D canvas) 
	{
		// Выбрать линию для рисования графика	
		canvas.setStroke(graphicsStroke);
		// Выбрать цвет линии
		canvas.setColor(Color.RED);
		/* Будем рисовать линию графика как путь, состоящий из множества сегментов (GeneralPath)
		 * Начало пути устанавливается в первую точку графика, после чего прямой соединяется со следующими точками*/
		
		GeneralPath graphics = new GeneralPath();
		
		for (int i=0; i<graphicsData.length; i++) 
		{
			// Преобразовать значения (x,y) в точку на экране point
			Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);	
			if (i>0) 
			{
				// Не первая итерация цикла - вести линию в точку point
				graphics.lineTo(point.getX(), point.getY());
			}
			else
			{
				// Первая итерация цикла - установить начало пути в точку point
				graphics.moveTo(point.getX(), point.getY());
			}
						
		}
		canvas.draw(graphics);		// Отобразить график
}
	
	private void graphics(BasicStroke basicStroke, boolean b) {
	}
	
	// Отображение маркеров точек, по которым рисовался график
	protected void paintMarkers(Graphics2D canvas) 
	{
		int i = 0;
		for (Double[] point: graphicsData) 		//Организовать цикл по всем точкам графика
		{
			canvas.setStroke(markerStroke);			//Установить специальное перо для черчения контуров маркеров
			
			
			//canvas.setPaint(Color.GREEN);			//Выбрать зелёный цвет для закрашивания маркеров внутри
			/*
				// Инициализировать эллипс как объект для представления маркера
			Ellipse2D.Double marker = new Ellipse2D.Double();
				// Эллипс будет задаваться посредством указания координат его центра и угла прямоугольника, в который он вписан
			Point2D.Double center = xyToPoint(point[0], point[1]);	// Центр - в точке (x,y)
			Point2D.Double corner = shiftPoint(center, 3, 3);		// Угол прямоугольника - отстоит на расстоянии (3,3)
			marker.setFrameFromCenter(center, corner);				// Задать эллипс по центру и диагонали
			canvas.draw(marker);									// Начертить контур маркера
			canvas.fill(marker);									// Залить внутреннюю область маркера		
			*/

			GeneralPath path = new GeneralPath();
			Point2D.Double center = xyToPoint(point[0], point[1]);
						
			canvas.setColor(Color.BLUE);	//Выбрать зелёный цвета для контуров маркеров		
			canvas.setPaint(Color.BLUE);
			if ( point[i].intValue() % 2 == 0 )
			{
				canvas.setColor(Color.GREEN);			//инеаче - синий
				canvas.setPaint(Color.GREEN);
			}
			
			/*
			path.append(new Line2D.Double(center.getX() - 2.5, center.getY() - 5.5, center.getX() + 5.5, center.getY() - 5.5), true);
			path.append(new Line2D.Double(center.getX() + 5.5, center.getY() - 5.5, center.getX() + 5.5, center.getY() + 5.5), true);
			path.append(new Line2D.Double(center.getX() + 5.5, center.getY() + 5.5, center.getX() - 5.5, center.getY() + 5.5), true);
			path.append(new Line2D.Double(center.getX() - 5.5, center.getY() + 5.5, center.getX() - 5.5, center.getY() - 5.5), true);
			path.append(new Line2D.Double(center.getX() - 5.5, center.getY() - 5.5, center.getX() + 5.5, center.getY() + 5.5), true);
			path.append(new Line2D.Double(center.getX() - 5.5, center.getY() + 5.5, center.getX() + 5.5, center.getY() - 5.5), true);
			*/
			path.append(new Line2D.Double(center.getX() - 2, center.getY() - 6, center.getX() + 2, center.getY() - 6), true);
			path.append(new Line2D.Double(center.getX() + 2, center.getY() - 6, center.getX() + 6, center.getY() - 2), true);
			path.append(new Line2D.Double(center.getX() + 6, center.getY() - 2, center.getX() + 6, center.getY() + 2), true);
			path.append(new Line2D.Double(center.getX() + 6, center.getY() + 2, center.getX() + 2, center.getY() + 6), true);
			path.append(new Line2D.Double(center.getX() + 2, center.getY() + 6, center.getX() - 2, center.getY() + 6), true);
			path.append(new Line2D.Double(center.getX() - 2, center.getY() + 6, center.getX() - 6, center.getY() + 2), true);
			path.append(new Line2D.Double(center.getX() - 6, center.getY() + 2, center.getX() - 6, center.getY() - 2), true);
			path.append(new Line2D.Double(center.getX() - 6, center.getY() - 2, center.getX() - 2, center.getY() - 6), true);
			
			path.append(new Line2D.Double(center.getX(), center.getY() - 6, center.getX(), center.getY() + 6), true);
			path.append(new Line2D.Double(center.getX() - 6, center.getY(), center.getX() + 6, center.getY()), true);
			
			
			canvas.draw(path);
		}
		
	}

	boolean unorderedValues( double value )
	{

int valueInt = (int) value;
{
int rest = valueInt%10;
valueInt = valueInt/10;
int restPrev = 9;
while(valueInt!=0){
restPrev = rest;
rest = valueInt%10;
if( restPrev < rest )
return false;
valueInt = valueInt/10;
}
}

valueInt = (int) value;
double rest = value - (double) valueInt;
int number = (int)(100*rest);
int numberPrev = (int)(10*rest);
for( int i = 0; i < Double.SIZE; i++ ){
if( numberPrev > number )
return false;
rest = 10*rest - (double)((int)(10*rest));
numberPrev = number;
number = (int)(10*rest);
}
return true;
	}
	
	private boolean unorderedValues(Double double1) {
		// TODO Auto-generated method stub
		return false;
	}
	// Метод, обеспечивающий отображение осей координат
	protected void paintAxis(Graphics2D canvas) 
	{
		// Установить особое начертание для осей
		canvas.setStroke(axisStroke);
		// Оси рисуются чѐрным цветом
		canvas.setColor(Color.BLACK);
		// Стрелки заливаются чѐрным цветом
		canvas.setPaint(Color.BLACK);
		// Подписи к координатным осям делаются специальным шрифтом 
		canvas.setFont(axisFont);
		
		
		//**************************************
		// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
		FontRenderContext context = canvas.getFontRenderContext();
		// Определить, должна ли быть видна ось Y на графике
		if (minX<=0.0 && maxX>=0.0) 
		{
			// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,// а правая (maxX) >= 0.0
			// Сама ось - это линия между точками (0, maxY) и (0, minY)
			canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
			// Стрелка оси Y
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на верхний конец оси Y
			Point2D.Double lineEnd = xyToPoint(0, maxY);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
			arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);
			// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
			arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());
			// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow);	// Закрасить стрелку
			
			// Нарисовать подпись к оси Y
			// Определить, сколько места понадобится для надписи "y"
			Rectangle2D bounds = axisFont.getStringBounds("y", context);
		
			Rectangle2D bounds00 = axisFont.getStringBounds("(0,0)", context);
			
			
			
		    
		     String label00 = "(0,0)";
		     //Rectangle2D bounds00 = myFont00.getStringBounds("(0,0)", context);
		     
		     Point2D.Double pointxy = xyToPoint(0, 0);
			
			Point2D.Double labelPos = xyToPoint(0, maxY);
			// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
			
			 canvas.drawString(label00, (float)(pointxy.getX() - bounds00.getWidth()/2), (float)(pointxy.getY() - bounds.getHeight()));
		}
		// Определить, должна ли быть видна ось X на графике
		if (minY<=0.0 && maxY>=0.0) 
		{
			// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
			// а нижняя (minY) <= 0.0
			canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
			// Стрелка оси X
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на правый конец оси X
			Point2D.Double lineEnd = xyToPoint(maxX, 0);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
			arrow.lineTo(arrow.getCurrentPoint().getX()-20, arrow.getCurrentPoint().getY()-5);
			// Вести левую часть стрелки в точку с относительными координатами (0, 10)
			arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY()+10);
			// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow);
			// Закрасить стрелку
			// Нарисовать подпись к оси X
			// Определить, сколько места понадобится для надписи "x"
			Rectangle2D bounds = axisFont.getStringBounds("x", context);
			Point2D.Double labelPos = xyToPoint(maxX, 0);
			// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("x", (float)(labelPos.getX() - bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
			}
	}
/* Метод-помощник, осуществляющий преобразование координат.Оно необходимо, т.к. верхнему левому углу холста с координатами
(0.0, 0.0) соответствует точка графика с координатами (minX, maxY),где  minX - это самое "левое" значение X, а maxY - самое "верхнее" значение Y.*/
	protected Point2D.Double xyToPoint(double x, double y) 
	{	
		// Вычисляем смещение X от самой левой точки (minX)
		double deltaX = x - minX;
		// Вычисляем смещение Y от точки верхней точки (maxY)
		double deltaY = maxY - y;
		return new Point2D.Double(deltaX*scale, deltaY*scale);
	}
	/* Метод-помощник, возвращающий экземпляр класса Point2D.Double
	 * смещѐнный по отношению к исходному на deltaX, deltaY
	 * К сожалению, стандартного метода, выполняющего такую задачу, нет.
	 */
	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) 
	{
		// Инициализировать новый экземпляр точки
		Point2D.Double dest = new Point2D.Double();
		// Задать еѐ координаты как координаты существующей точки + заданные смещения
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
		return dest;
	}
}
